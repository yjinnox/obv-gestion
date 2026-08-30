package com.obvgestion.application.vente;

import com.obvgestion.application.referentiel.ConditionnementRepository;
import com.obvgestion.application.referentiel.ProduitRepository;
import com.obvgestion.application.referentiel.TarifRepository;
import com.obvgestion.application.stock.StockRepository;
import com.obvgestion.domain.referentiel.NatureTarif;
import com.obvgestion.domain.referentiel.Produit;
import com.obvgestion.domain.referentiel.UniteVente;
import com.obvgestion.domain.vente.LignePanier;
import com.obvgestion.domain.vente.Panier;
import com.obvgestion.domain.vente.SessionVente;
import com.obvgestion.domain.vente.VenteInvalideException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * §8.2 — panier de vente au dépôt, persisté en Redis (TTL 4h, RG-25 : sans
 * effet sur le stock ni la base tant qu'il n'est pas commandé).
 */
@Service
public class PanierService {

    private final PanierRepository panierRepository;
    private final SessionVenteRepository sessionVenteRepository;
    private final ProduitRepository produitRepository;
    private final ConditionnementRepository conditionnementRepository;
    private final StockRepository stockRepository;
    private final TarifRepository tarifRepository;

    public PanierService(PanierRepository panierRepository, SessionVenteRepository sessionVenteRepository,
                          ProduitRepository produitRepository, ConditionnementRepository conditionnementRepository,
                          StockRepository stockRepository, TarifRepository tarifRepository) {
        this.panierRepository = panierRepository;
        this.sessionVenteRepository = sessionVenteRepository;
        this.produitRepository = produitRepository;
        this.conditionnementRepository = conditionnementRepository;
        this.stockRepository = stockRepository;
        this.tarifRepository = tarifRepository;
    }

    @Transactional(readOnly = true)
    public Panier obtenir(Long utilisateurId, Long sessionVenteId) {
        return panierRepository.trouver(utilisateurId, sessionVenteId);
    }

    /** §8.2 étape 3 — écran panier : lignes avec PU et total ligne (prix courant, pas encore figé). */
    @Transactional(readOnly = true)
    public PanierDetaille obtenirDetaille(Long utilisateurId, Long sessionVenteId) {
        Panier panier = panierRepository.trouver(utilisateurId, sessionVenteId);
        SessionVente session = sessionVenteRepository.parId(sessionVenteId)
                .orElseThrow(() -> new NoSuchElementException("Session de vente introuvable : " + sessionVenteId));

        List<LignePanierDetaillee> lignes = panier.lignes().stream().map(ligne -> detaillerLigne(session, ligne)).toList();
        long montantGlobal = lignes.stream().mapToLong(LignePanierDetaillee::montantLigneXof).sum();
        return new PanierDetaille(utilisateurId, sessionVenteId, lignes, montantGlobal);
    }

    private LignePanierDetaillee detaillerLigne(SessionVente session, LignePanier ligne) {
        Produit produit = produitRepository.parId(ligne.produitId())
                .orElseThrow(() -> new NoSuchElementException("Produit introuvable : " + ligne.produitId()));
        long prixCasier = tarifRepository.tarifOuvert(session.getPointDeVente().getId(), produit.getId(),
                        UniteVente.CASIER, NatureTarif.VENTE)
                .map(tarif -> tarif.getMontant().valeurXof())
                .orElse(0L);
        long montantLigne = prixCasier * ligne.quantiteDemiCasiers() / 2;
        return new LignePanierDetaillee(ligne.id(), produit.getId(), produit.getMarque().getLibelle(),
                produit.getVolume().getLibelle(), ligne.quantiteDemiCasiers(), prixCasier, montantLigne);
    }

    /** RG-13 (demi-casier autorisé) + RG-24 (contrôle de disponibilité informatif, non bloquant). */
    @Transactional(readOnly = true)
    public ResultatAjoutPanier ajouterLigne(Long utilisateurId, Long sessionVenteId, Long produitId,
                                             long quantiteDemiCasiers) {
        if (quantiteDemiCasiers <= 0) {
            throw new VenteInvalideException("La quantité doit être strictement positive.");
        }
        Produit produit = produitRepository.parId(produitId)
                .orElseThrow(() -> new NoSuchElementException("Produit introuvable : " + produitId));
        exigerDemiCasierAutoriseSiNecessaire(produit, quantiteDemiCasiers);

        Panier panier = panierRepository.trouver(utilisateurId, sessionVenteId)
                .avecLigneAjoutee(produitId, quantiteDemiCasiers);
        panierRepository.enregistrer(panier);

        long stockDisponible = stockDisponible(sessionVenteId, produitId);
        return new ResultatAjoutPanier(panier, stockDisponible);
    }

    @Transactional(readOnly = true)
    public Panier modifierLigne(Long utilisateurId, Long sessionVenteId, int ligneId, long quantiteDemiCasiers) {
        if (quantiteDemiCasiers <= 0) {
            throw new VenteInvalideException("La quantité doit être strictement positive.");
        }
        Panier panier = panierRepository.trouver(utilisateurId, sessionVenteId).avecLigneModifiee(ligneId, quantiteDemiCasiers);
        panierRepository.enregistrer(panier);
        return panier;
    }

    @Transactional(readOnly = true)
    public Panier supprimerLigne(Long utilisateurId, Long sessionVenteId, int ligneId) {
        Panier panier = panierRepository.trouver(utilisateurId, sessionVenteId).sansLigne(ligneId);
        panierRepository.enregistrer(panier);
        return panier;
    }

    /** RG-25 — aucun effet sur le stock ni la base : simple suppression Redis. */
    @Transactional(readOnly = true)
    public void vider(Long utilisateurId, Long sessionVenteId) {
        panierRepository.supprimer(utilisateurId, sessionVenteId);
    }

    private void exigerDemiCasierAutoriseSiNecessaire(Produit produit, long quantiteDemiCasiers) {
        if (quantiteDemiCasiers % 2 == 0) {
            return;
        }
        boolean autorise = conditionnementRepository.rechercher(produit.getId(), true, Pageable.unpaged())
                .stream().anyMatch(conditionnement -> conditionnement.isDemiCasierAutorise());
        if (!autorise) {
            throw new VenteInvalideException(
                    "Le demi-casier n'est pas autorisé pour ce produit (RG-13).");
        }
    }

    private long stockDisponible(Long sessionVenteId, Long produitId) {
        Long pointDeVenteId = sessionVenteRepository.parId(sessionVenteId)
                .map(session -> session.getPointDeVente().getId())
                .orElse(null);
        if (pointDeVenteId == null) {
            return 0;
        }
        return stockRepository.parPointDeVenteEtProduit(pointDeVenteId, produitId)
                .map(stock -> stock.getQuantite())
                .orElse(0L);
    }
}
