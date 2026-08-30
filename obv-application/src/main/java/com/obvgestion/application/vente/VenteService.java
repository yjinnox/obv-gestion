package com.obvgestion.application.vente;

import com.obvgestion.application.referentiel.ClientService;
import com.obvgestion.application.referentiel.ProduitRepository;
import com.obvgestion.application.referentiel.TarifRepository;
import com.obvgestion.application.stock.StockRepository;
import com.obvgestion.application.stock.StockService;
import com.obvgestion.application.utilisateur.UtilisateurRepository;
import com.obvgestion.domain.commun.Montant;
import com.obvgestion.domain.referentiel.Client;
import com.obvgestion.domain.referentiel.NatureTarif;
import com.obvgestion.domain.referentiel.Produit;
import com.obvgestion.domain.referentiel.UniteVente;
import com.obvgestion.domain.stock.StockInsuffisantException;
import com.obvgestion.domain.stock.TypeMouvementStock;
import com.obvgestion.domain.utilisateur.Utilisateur;
import com.obvgestion.domain.vente.LignePanier;
import com.obvgestion.domain.vente.LigneVente;
import com.obvgestion.domain.vente.LigneVenteDemandee;
import com.obvgestion.domain.vente.ModePaiement;
import com.obvgestion.domain.vente.Panier;
import com.obvgestion.domain.vente.SessionVente;
import com.obvgestion.domain.vente.TypeNumeroDocument;
import com.obvgestion.domain.vente.Vente;
import com.obvgestion.domain.vente.VenteInvalideException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/** §8.2 — validation du panier et commande (RG-24 à RG-27). */
@Service
public class VenteService {

    private static final String TYPE_DOCUMENT = "VENTE";

    private final VenteRepository venteRepository;
    private final SessionVenteRepository sessionVenteRepository;
    private final PanierRepository panierRepository;
    private final ClientService clientService;
    private final ProduitRepository produitRepository;
    private final TarifRepository tarifRepository;
    private final CompteurDocumentRepository compteurDocumentRepository;
    private final StockRepository stockRepository;
    private final StockService stockService;
    private final UtilisateurRepository utilisateurRepository;
    private final int tauxTvaPourcent;
    private final ZoneId fuseauHoraireMetier;

    public VenteService(VenteRepository venteRepository, SessionVenteRepository sessionVenteRepository,
                         PanierRepository panierRepository, ClientService clientService,
                         ProduitRepository produitRepository, TarifRepository tarifRepository,
                         CompteurDocumentRepository compteurDocumentRepository, StockRepository stockRepository,
                         StockService stockService, UtilisateurRepository utilisateurRepository,
                         @Value("${entreprise.taux-tva-pourcent}") int tauxTvaPourcent,
                         @Value("${app.timezone}") String fuseauHoraireMetier) {
        this.venteRepository = venteRepository;
        this.sessionVenteRepository = sessionVenteRepository;
        this.panierRepository = panierRepository;
        this.clientService = clientService;
        this.produitRepository = produitRepository;
        this.tarifRepository = tarifRepository;
        this.compteurDocumentRepository = compteurDocumentRepository;
        this.stockRepository = stockRepository;
        this.stockService = stockService;
        this.utilisateurRepository = utilisateurRepository;
        this.tauxTvaPourcent = tauxTvaPourcent;
        this.fuseauHoraireMetier = ZoneId.of(fuseauHoraireMetier);
    }

    /**
     * §8.2 étape 6 — « Commander ». RG-27 : idempotente sur
     * {@code idempotencyKey} (double clic) ; RG-26 : numérotation BC/FA ;
     * RG-24 : contrôle de stock bloquant, décrémenté ici (jamais à l'ajout
     * au panier).
     */
    @Transactional
    public Vente commander(Long sessionVenteId, Long utilisateurId, Long clientId,
                            NouveauClientCommande nouveauClient, ModePaiement modePaiement, String idempotencyKey) {
        Optional<Vente> existante = venteRepository.parIdempotencyKey(sessionVenteId, idempotencyKey);
        if (existante.isPresent()) {
            return existante.get();
        }

        SessionVente session = sessionVenteRepository.parId(sessionVenteId)
                .orElseThrow(() -> new NoSuchElementException("Session de vente introuvable : " + sessionVenteId));
        Panier panier = panierRepository.trouver(utilisateurId, sessionVenteId);
        if (panier.lignes().isEmpty()) {
            throw new VenteInvalideException("Le panier est vide.");
        }

        Client client = resoudreClient(clientId, nouveauClient);
        List<LigneVenteDemandee> lignesDemandees = resoudreLignes(session, panier);
        verifierDisponibilite(session.getPointDeVente().getId(), lignesDemandees);

        int annee = ZonedDateTime.now(fuseauHoraireMetier).getYear();
        String numeroBonCommande = compteurDocumentRepository.prochainNumero(
                session.getPointDeVente(), TypeNumeroDocument.BON_COMMANDE, annee);
        String numeroFacture = compteurDocumentRepository.prochainNumero(
                session.getPointDeVente(), TypeNumeroDocument.FACTURE, annee);

        Vente vente;
        try {
            vente = venteRepository.enregistrerEtValider(Vente.commander(session, client, numeroBonCommande,
                    numeroFacture, modePaiement, lignesDemandees, tauxTvaPourcent, idempotencyKey, Instant.now()));
        } catch (DataIntegrityViolationException e) {
            return venteRepository.parIdempotencyKey(sessionVenteId, idempotencyKey).orElseThrow(() -> e);
        }

        Utilisateur acteur = utilisateurRepository.parId(utilisateurId)
                .orElseThrow(() -> new NoSuchElementException("Utilisateur introuvable : " + utilisateurId));
        for (LigneVente ligne : vente.getLignes()) {
            stockService.appliquer(session.getPointDeVente(), ligne.getProduit(), TypeMouvementStock.SORTIE_VENTE,
                    -ligne.quantiteDemiCasiers(), TYPE_DOCUMENT, vente.getId(), acteur);
        }

        panierRepository.supprimer(utilisateurId, sessionVenteId);
        return vente;
    }

    private Client resoudreClient(Long clientId, NouveauClientCommande nouveauClient) {
        if (clientId != null) {
            return clientService.trouver(clientId);
        }
        if (nouveauClient == null) {
            throw new VenteInvalideException("Un client existant ou de nouvelles informations client sont requis.");
        }
        return clientService.creer(nouveauClient.type(), nouveauClient.nom(), nouveauClient.prenoms(),
                nouveauClient.raisonSociale(), nouveauClient.telephone(), nouveauClient.email(),
                nouveauClient.adresseFacturation());
    }

    private List<LigneVenteDemandee> resoudreLignes(SessionVente session, Panier panier) {
        List<LigneVenteDemandee> lignes = new ArrayList<>();
        for (LignePanier lignePanier : panier.lignes()) {
            Produit produit = produitRepository.parId(lignePanier.produitId())
                    .orElseThrow(() -> new NoSuchElementException("Produit introuvable : " + lignePanier.produitId()));
            Montant prixVente = tarifRepository.tarifOuvert(session.getPointDeVente().getId(), produit.getId(),
                            UniteVente.CASIER, NatureTarif.VENTE)
                    .map(tarif -> tarif.getMontant())
                    .orElseThrow(() -> new VenteInvalideException(
                            "Aucun tarif de vente en vigueur pour le produit " + produit.getId() + "."));
            lignes.add(new LigneVenteDemandee(produit, lignePanier.quantiteDemiCasiers(), prixVente,
                    produit.getMontantConsigne()));
        }
        return lignes;
    }

    /** RG-24 — contrôle bloquant anticipé : évite de décrémenter partiellement un panier multi-lignes. */
    private void verifierDisponibilite(Long pointDeVenteId, List<LigneVenteDemandee> lignesDemandees) {
        for (LigneVenteDemandee ligne : lignesDemandees) {
            long disponible = stockRepository.parPointDeVenteEtProduit(pointDeVenteId, ligne.produit().getId())
                    .map(stock -> stock.getQuantite())
                    .orElse(0L);
            if (disponible < ligne.quantiteDemiCasiers()) {
                throw new StockInsuffisantException(
                        ligne.produit().getMarque().getLibelle() + " " + ligne.produit().getVolume().getLibelle(),
                        ligne.quantiteDemiCasiers(), disponible);
            }
        }
    }

    @Transactional(readOnly = true)
    public Vente trouver(Long id) {
        return venteRepository.parId(id).orElseThrow(() -> new NoSuchElementException("Vente introuvable : " + id));
    }

    @Transactional(readOnly = true)
    public Page<Vente> rechercher(Long sessionVenteId, Pageable pageable) {
        return venteRepository.rechercher(sessionVenteId, pageable);
    }
}
