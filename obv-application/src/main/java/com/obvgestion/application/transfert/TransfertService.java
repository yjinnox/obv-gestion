package com.obvgestion.application.transfert;

import com.obvgestion.application.audit.Journalisateur;
import com.obvgestion.application.referentiel.ConditionnementRepository;
import com.obvgestion.application.referentiel.PointDeVenteRepository;
import com.obvgestion.application.referentiel.ProduitRepository;
import com.obvgestion.application.referentiel.TarifRepository;
import com.obvgestion.application.stock.StockRepository;
import com.obvgestion.application.stock.StockService;
import com.obvgestion.application.utilisateur.UtilisateurRepository;
import com.obvgestion.application.vente.CompteurDocumentRepository;
import com.obvgestion.domain.audit.TypeActionJournal;
import com.obvgestion.domain.commun.Montant;
import com.obvgestion.domain.referentiel.Conditionnement;
import com.obvgestion.domain.referentiel.NatureTarif;
import com.obvgestion.domain.referentiel.PointDeVente;
import com.obvgestion.domain.referentiel.Produit;
import com.obvgestion.domain.referentiel.UniteVente;
import com.obvgestion.domain.stock.MouvementDemande;
import com.obvgestion.domain.stock.StockInsuffisantException;
import com.obvgestion.domain.stock.TypeMouvementStock;
import com.obvgestion.domain.transfert.BonTransfert;
import com.obvgestion.domain.transfert.LigneTransfert;
import com.obvgestion.domain.transfert.StatutTransfert;
import com.obvgestion.domain.transfert.TransfertInvalideException;
import com.obvgestion.domain.utilisateur.Utilisateur;
import com.obvgestion.domain.vente.TypeNumeroDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.NoSuchElementException;

/** §9 — cycle de vie complet d'un transfert dépôt → bar (RG-01, RG-30 à RG-32). */
@Service
public class TransfertService {

    private static final String TYPE_DOCUMENT = "TRANSFERT";

    private final BonTransfertRepository bonTransfertRepository;
    private final PointDeVenteRepository pointDeVenteRepository;
    private final ProduitRepository produitRepository;
    private final ConditionnementRepository conditionnementRepository;
    private final TarifRepository tarifRepository;
    private final CompteurDocumentRepository compteurDocumentRepository;
    private final StockRepository stockRepository;
    private final StockService stockService;
    private final UtilisateurRepository utilisateurRepository;
    private final Journalisateur journalisateur;
    private final ZoneId fuseauHoraireMetier;

    public TransfertService(BonTransfertRepository bonTransfertRepository,
                             PointDeVenteRepository pointDeVenteRepository, ProduitRepository produitRepository,
                             ConditionnementRepository conditionnementRepository, TarifRepository tarifRepository,
                             CompteurDocumentRepository compteurDocumentRepository, StockRepository stockRepository,
                             StockService stockService, UtilisateurRepository utilisateurRepository,
                             Journalisateur journalisateur, @Value("${app.timezone}") String fuseauHoraireMetier) {
        this.bonTransfertRepository = bonTransfertRepository;
        this.pointDeVenteRepository = pointDeVenteRepository;
        this.produitRepository = produitRepository;
        this.conditionnementRepository = conditionnementRepository;
        this.tarifRepository = tarifRepository;
        this.compteurDocumentRepository = compteurDocumentRepository;
        this.stockRepository = stockRepository;
        this.stockService = stockService;
        this.utilisateurRepository = utilisateurRepository;
        this.journalisateur = journalisateur;
        this.fuseauHoraireMetier = ZoneId.of(fuseauHoraireMetier);
    }

    /** §9 — création en une fois (numérotation RG-26, lignes fournies en bloc, prix de cession préfilable). */
    @Transactional
    public BonTransfert creer(Long pointDeVenteSourceId, Long pointDeVenteDestinationId, Instant dateHeure,
                               List<LigneTransfertDemandee> lignesDemandees) {
        PointDeVente source = pointDeVenteRepository.parId(pointDeVenteSourceId)
                .orElseThrow(() -> new NoSuchElementException("Point de vente introuvable : " + pointDeVenteSourceId));
        PointDeVente destination = pointDeVenteRepository.parId(pointDeVenteDestinationId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Point de vente introuvable : " + pointDeVenteDestinationId));

        int annee = ZonedDateTime.now(fuseauHoraireMetier).getYear();
        String numero = compteurDocumentRepository.prochainNumero(source, TypeNumeroDocument.TRANSFERT, annee);

        BonTransfert transfert = new BonTransfert(numero, source, destination, dateHeure);
        for (LigneTransfertDemandee demande : lignesDemandees) {
            Produit produit = produitRepository.parId(demande.produitId())
                    .orElseThrow(() -> new NoSuchElementException("Produit introuvable : " + demande.produitId()));
            Conditionnement conditionnement = conditionnementRepository.parId(demande.conditionnementId())
                    .orElseThrow(() -> new NoSuchElementException(
                            "Conditionnement introuvable : " + demande.conditionnementId()));
            Montant prix = demande.prixCessionCasierXof() != null
                    ? new Montant(demande.prixCessionCasierXof())
                    : prixCessionEnVigueur(source.getId(), produit.getId());
            transfert.ajouterLigne(produit, conditionnement, demande.quantiteDemiCasiers(), prix);
        }
        return bonTransfertRepository.enregistrer(transfert);
    }

    private Montant prixCessionEnVigueur(Long pointDeVenteSourceId, Long produitId) {
        return tarifRepository.tarifOuvert(pointDeVenteSourceId, produitId, UniteVente.CASIER, NatureTarif.CESSION)
                .map(tarif -> tarif.getMontant())
                .orElseThrow(() -> new TransfertInvalideException(
                        "Aucun tarif de cession en vigueur pour ce produit : veuillez saisir un prix."));
    }

    /** RG-31 — clôture : bascule en attente de validation et mouvemente les deux stocks de façon atomique par ligne. */
    @Transactional
    public BonTransfert cloturer(Long transfertId, Long acteurId) {
        BonTransfert transfert = trouver(transfertId);
        Utilisateur acteur = utilisateur(acteurId);
        verifierDisponibiliteSource(transfert);

        transfert.cloturer(acteurId.toString());
        transfert = bonTransfertRepository.enregistrer(transfert);

        for (LigneTransfert ligne : transfert.getLignes()) {
            stockService.appliquerPlusieurs(List.of(
                    new MouvementDemande(transfert.getPointDeVenteSource(), ligne.getProduit(),
                            TypeMouvementStock.SORTIE_TRANSFERT, -ligne.getQuantiteDemiCasiers()),
                    new MouvementDemande(transfert.getPointDeVenteDestination(), ligne.getProduit(),
                            TypeMouvementStock.ENTREE_TRANSFERT, ligne.getQuantiteBouteilles())),
                    TYPE_DOCUMENT, transfert.getId(), acteur);
        }
        return transfert;
    }

    /** RG-24 (par analogie) — contrôle bloquant anticipé : évite de décrémenter partiellement un transfert multi-lignes. */
    private void verifierDisponibiliteSource(BonTransfert transfert) {
        for (LigneTransfert ligne : transfert.getLignes()) {
            long disponible = stockRepository
                    .parPointDeVenteEtProduit(transfert.getPointDeVenteSource().getId(), ligne.getProduit().getId())
                    .map(stock -> stock.getQuantite())
                    .orElse(0L);
            if (disponible < ligne.getQuantiteDemiCasiers()) {
                throw new StockInsuffisantException(
                        ligne.getProduit().getMarque().getLibelle() + " " + ligne.getProduit().getVolume().getLibelle(),
                        ligne.getQuantiteDemiCasiers(), disponible);
            }
        }
    }

    /** RG-01 — un utilisateur ne peut jamais valider un document qu'il a lui-même clôturé. */
    @Transactional
    public void valider(Long transfertId, Long acteurId, String adresseIp) {
        BonTransfert transfert = trouver(transfertId);
        transfert.valider(acteurId.toString());
        bonTransfertRepository.enregistrer(transfert);
        journalisateur.journaliser(acteurId.toString(), TypeActionJournal.VALIDATION, "BonTransfert",
                transfertId.toString(), StatutTransfert.EN_ATTENTE_VALIDATION.name(), StatutTransfert.VALIDEE.name(),
                adresseIp);
    }

    /** RG-18 (par analogie) — annulation logique : contre-passation atomique des deux stocks, motif obligatoire. */
    @Transactional
    public void annuler(Long transfertId, String motif, Long acteurId, String adresseIp) {
        BonTransfert transfert = trouver(transfertId);
        Utilisateur acteur = utilisateur(acteurId);

        transfert.annuler(motif);
        bonTransfertRepository.enregistrer(transfert);

        for (LigneTransfert ligne : transfert.getLignes()) {
            stockService.appliquerPlusieurs(List.of(
                    new MouvementDemande(transfert.getPointDeVenteSource(), ligne.getProduit(),
                            TypeMouvementStock.CONTRE_PASSATION, ligne.getQuantiteDemiCasiers()),
                    new MouvementDemande(transfert.getPointDeVenteDestination(), ligne.getProduit(),
                            TypeMouvementStock.CONTRE_PASSATION, -ligne.getQuantiteBouteilles())),
                    TYPE_DOCUMENT, transfert.getId(), acteur);
        }
        journalisateur.journaliser(acteurId.toString(), TypeActionJournal.ANNULATION, "BonTransfert",
                transfertId.toString(), StatutTransfert.EN_ATTENTE_VALIDATION.name(), motif, adresseIp);
    }

    @Transactional(readOnly = true)
    public BonTransfert trouver(Long id) {
        return bonTransfertRepository.parId(id)
                .orElseThrow(() -> new NoSuchElementException("Transfert introuvable : " + id));
    }

    @Transactional(readOnly = true)
    public Page<BonTransfert> rechercher(Long pointDeVenteSourceId, Long pointDeVenteDestinationId,
                                          StatutTransfert statut, Pageable pageable) {
        return bonTransfertRepository.rechercher(pointDeVenteSourceId, pointDeVenteDestinationId, statut, pageable);
    }

    private Utilisateur utilisateur(Long id) {
        return utilisateurRepository.parId(id)
                .orElseThrow(() -> new NoSuchElementException("Utilisateur introuvable : " + id));
    }
}
