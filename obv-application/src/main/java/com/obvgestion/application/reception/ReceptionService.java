package com.obvgestion.application.reception;

import com.obvgestion.application.audit.Journalisateur;
import com.obvgestion.application.notification.Notification;
import com.obvgestion.application.notification.NotificationService;
import com.obvgestion.application.referentiel.ConditionnementRepository;
import com.obvgestion.application.referentiel.FournisseurRepository;
import com.obvgestion.application.referentiel.PointDeVenteRepository;
import com.obvgestion.application.referentiel.ProduitRepository;
import com.obvgestion.application.referentiel.TarifRepository;
import com.obvgestion.application.stock.StockService;
import com.obvgestion.application.utilisateur.UtilisateurRepository;
import com.obvgestion.domain.audit.TypeActionJournal;
import com.obvgestion.domain.commun.JetonOpaque;
import com.obvgestion.domain.commun.Montant;
import com.obvgestion.domain.commun.SeparationDesTachesException;
import com.obvgestion.domain.reception.JetonValidationReception;
import com.obvgestion.domain.reception.LigneReception;
import com.obvgestion.domain.reception.Reception;
import com.obvgestion.domain.reception.ReceptionInvalideException;
import com.obvgestion.domain.reception.StatutReception;
import com.obvgestion.domain.referentiel.Conditionnement;
import com.obvgestion.domain.referentiel.Fournisseur;
import com.obvgestion.domain.referentiel.NatureTarif;
import com.obvgestion.domain.referentiel.PointDeVente;
import com.obvgestion.domain.referentiel.Produit;
import com.obvgestion.domain.referentiel.UniteVente;
import com.obvgestion.domain.stock.TypeMouvementStock;
import com.obvgestion.domain.utilisateur.Permission;
import com.obvgestion.domain.utilisateur.RoleUtilisateur;
import com.obvgestion.domain.utilisateur.Utilisateur;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * §7 — cycle de vie complet d'une réception au dépôt (RG-01, RG-17 à RG-22).
 */
@Service
public class ReceptionService {

    private static final String TYPE_DOCUMENT = "RECEPTION";

    private final ReceptionRepository receptionRepository;
    private final JetonValidationReceptionRepository jetonValidationReceptionRepository;
    private final FournisseurRepository fournisseurRepository;
    private final PointDeVenteRepository pointDeVenteRepository;
    private final ProduitRepository produitRepository;
    private final ConditionnementRepository conditionnementRepository;
    private final TarifRepository tarifRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final StockService stockService;
    private final Journalisateur journalisateur;
    private final NotificationService notificationService;
    private final String urlFrontend;

    public ReceptionService(ReceptionRepository receptionRepository,
                             JetonValidationReceptionRepository jetonValidationReceptionRepository,
                             FournisseurRepository fournisseurRepository,
                             PointDeVenteRepository pointDeVenteRepository,
                             ProduitRepository produitRepository,
                             ConditionnementRepository conditionnementRepository,
                             TarifRepository tarifRepository,
                             UtilisateurRepository utilisateurRepository,
                             StockService stockService,
                             Journalisateur journalisateur,
                             NotificationService notificationService,
                             @Value("${app.frontend-url}") String urlFrontend) {
        this.receptionRepository = receptionRepository;
        this.jetonValidationReceptionRepository = jetonValidationReceptionRepository;
        this.fournisseurRepository = fournisseurRepository;
        this.pointDeVenteRepository = pointDeVenteRepository;
        this.produitRepository = produitRepository;
        this.conditionnementRepository = conditionnementRepository;
        this.tarifRepository = tarifRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.stockService = stockService;
        this.journalisateur = journalisateur;
        this.notificationService = notificationService;
        this.urlFrontend = urlFrontend;
    }

    /** §7.2 étape 1. */
    @Transactional
    public Reception creer(Long fournisseurId, Long pointDeVenteId, Instant dateHeureLivraison) {
        Fournisseur fournisseur = fournisseurRepository.parId(fournisseurId)
                .orElseThrow(() -> new NoSuchElementException("Fournisseur introuvable : " + fournisseurId));
        PointDeVente pointDeVente = pointDeVenteRepository.parId(pointDeVenteId)
                .orElseThrow(() -> new NoSuchElementException("Point de vente introuvable : " + pointDeVenteId));
        return receptionRepository.enregistrer(new Reception(fournisseur, pointDeVente, dateHeureLivraison));
    }

    /**
     * §7.2 étape 2 — {@code prixAchatCasier} nullable : pré-rempli depuis le
     * tarif ACHAT en vigueur si omis (modifiable en le renseignant).
     */
    @Transactional
    public LigneReception ajouterLigne(Long receptionId, Long produitId, Long conditionnementId, long nombreCasiers,
                                        Montant prixAchatCasier) {
        Reception reception = trouver(receptionId);
        Produit produit = produitRepository.parId(produitId)
                .orElseThrow(() -> new NoSuchElementException("Produit introuvable : " + produitId));
        Conditionnement conditionnement = conditionnementRepository.parId(conditionnementId)
                .orElseThrow(() -> new NoSuchElementException("Conditionnement introuvable : " + conditionnementId));
        Montant prix = prixAchatCasier != null
                ? prixAchatCasier : prixAchatEnVigueur(reception.getPointDeVente().getId(), produitId);

        LigneReception ligne = reception.ajouterLigne(produit, conditionnement, nombreCasiers, prix);
        receptionRepository.enregistrer(reception);
        return ligne;
    }

    private Montant prixAchatEnVigueur(Long pointDeVenteId, Long produitId) {
        return tarifRepository.tarifOuvert(pointDeVenteId, produitId, UniteVente.CASIER, NatureTarif.ACHAT)
                .map(tarif -> tarif.getMontant())
                .orElseThrow(() -> new ReceptionInvalideException(
                        "Aucun tarif d'achat en vigueur pour ce produit : veuillez saisir un prix."));
    }

    /**
     * RG-20 — modifier une ligne d'une réception déjà en attente de
     * validation ajuste le stock (déjà incrémenté à la clôture, RG-17) de
     * l'écart et journalise la correction ; en brouillon, la ligne n'a pas
     * encore d'impact sur le stock. RG-20 réserve cette correction au
     * SUPER_ADMINISTRATEUR ({@code MODIFICATION_POST_CLOTURE}) : une fois la
     * réception clôturée, {@code RECEPTION_WRITE} seul (détenu par le
     * gérant qui l'a préparée) ne suffit plus.
     */
    @Transactional
    public void modifierLigne(Long receptionId, Long ligneId, long nombreCasiers, Montant prixAchatCasier,
                               Long acteurId, String adresseIp) {
        Reception reception = trouver(receptionId);
        boolean stockDejaImpacte = reception.getStatut() == StatutReception.EN_ATTENTE_VALIDATION;
        Utilisateur acteur = utilisateur(acteurId);
        if (stockDejaImpacte && !acteur.permissions().contains(Permission.MODIFICATION_POST_CLOTURE)) {
            throw new SeparationDesTachesException(
                    "Seul un SUPER_ADMINISTRATEUR peut modifier une réception déjà en attente de validation.");
        }
        LigneReception ligne = ligneDe(reception, ligneId);
        long demiCasiersAvant = ligne.quantiteDemiCasiers();
        String descriptionAvant = decrireLigne(ligne);

        reception.modifierLigne(ligneId, nombreCasiers, prixAchatCasier);
        receptionRepository.enregistrer(reception);

        if (stockDejaImpacte) {
            long delta = ligne.quantiteDemiCasiers() - demiCasiersAvant;
            if (delta != 0) {
                stockService.appliquer(reception.getPointDeVente(), ligne.getProduit(), TypeMouvementStock.AJUSTEMENT,
                        delta, TYPE_DOCUMENT, reception.getId(), acteur);
            }
            journalisateur.journaliser(acteurId.toString(), TypeActionJournal.MODIFICATION_POST_CLOTURE,
                    "LigneReception", ligneId.toString(), descriptionAvant, decrireLigne(ligne), adresseIp);
        }
    }

    /** §13 — suppression réservée au brouillon (RG-20 : en attente de validation, corriger la quantité à la place). */
    @Transactional
    public void supprimerLigne(Long receptionId, Long ligneId) {
        Reception reception = trouver(receptionId);
        reception.supprimerLigne(ligneId);
        receptionRepository.enregistrer(reception);
    }

    /** RG-17 — clôture : bascule en attente de validation et incrémente le stock (ENTREE_RECEPTION). */
    @Transactional
    public Reception cloturer(Long receptionId, Long acteurId) {
        Reception reception = trouver(receptionId);
        Utilisateur acteur = utilisateur(acteurId);

        reception.cloturer(acteurId.toString());
        reception = receptionRepository.enregistrer(reception);

        for (LigneReception ligne : reception.getLignes()) {
            stockService.appliquer(reception.getPointDeVente(), ligne.getProduit(), TypeMouvementStock.ENTREE_RECEPTION,
                    ligne.quantiteDemiCasiers(), TYPE_DOCUMENT, reception.getId(), acteur);
        }
        return reception;
    }

    /** §7.2 étapes 4-5 — sélection du SUPER_ADMINISTRATEUR destinataire et notification (RG-35/RG-36). */
    @Transactional
    public void demanderValidation(Long receptionId, Long destinataireId) {
        Reception reception = trouver(receptionId);
        if (reception.getStatut() != StatutReception.EN_ATTENTE_VALIDATION) {
            throw new ReceptionInvalideException(
                    "Une demande de validation nécessite une réception en attente de validation.");
        }
        Utilisateur destinataire = utilisateur(destinataireId);
        if (!destinataire.possedeRole(RoleUtilisateur.SUPER_ADMINISTRATEUR)) {
            throw new ReceptionInvalideException(
                    "Seul un SUPER_ADMINISTRATEUR peut être sélectionné pour valider une réception.");
        }

        JetonOpaque jeton = JetonOpaque.genererAleatoire();
        jetonValidationReceptionRepository.enregistrer(
                JetonValidationReception.creer(reception, destinataire, jeton.hacher(), Instant.now()));

        // L'écran de détail d'une réception affiche déjà son récapitulatif
        // (totaux par marque/volume, §7.2 étape 3) et les actions de
        // validation : c'est lui la cible du lien, il n'existe pas de route
        // « /recapitulatif » distincte côté frontend.
        String lien = urlFrontend + "/receptions/" + reception.getId() + "?token=" + jeton.valeurClaire();
        notificationService.envoyer(new Notification(
                destinataire.canalNotification(), destinataire.contactNotification(), "reception-demande-validation",
                Map.of("nom", destinataire.getNom(), "prenoms", destinataire.getPrenoms(),
                        "fournisseur", reception.getFournisseur().getRaisonSociale(),
                        "montantTotalXof", reception.montantTotal().valeurXof(),
                        "lienRecapitulatif", lien)));
    }

    /** RG-01 — un utilisateur ne peut jamais valider un document qu'il a lui-même clôturé. */
    @Transactional
    public void valider(Long receptionId, Long acteurId, String adresseIp) {
        Reception reception = trouver(receptionId);
        reception.valider(acteurId.toString());
        receptionRepository.enregistrer(reception);
        journalisateur.journaliser(acteurId.toString(), TypeActionJournal.VALIDATION, "Reception",
                receptionId.toString(), StatutReception.EN_ATTENTE_VALIDATION.name(),
                StatutReception.VALIDEE.name(), adresseIp);
    }

    /** RG-18/RG-19/RG-22 — annulation logique : contre-passation du stock, motif obligatoire. */
    @Transactional
    public void annuler(Long receptionId, String motif, Long acteurId, String adresseIp) {
        Reception reception = trouver(receptionId);
        Utilisateur acteur = utilisateur(acteurId);

        reception.annuler(motif);
        receptionRepository.enregistrer(reception);

        for (LigneReception ligne : reception.getLignes()) {
            stockService.appliquer(reception.getPointDeVente(), ligne.getProduit(), TypeMouvementStock.CONTRE_PASSATION,
                    -ligne.quantiteDemiCasiers(), TYPE_DOCUMENT, reception.getId(), acteur);
        }
        journalisateur.journaliser(acteurId.toString(), TypeActionJournal.ANNULATION, "Reception",
                receptionId.toString(), StatutReception.EN_ATTENTE_VALIDATION.name(), motif, adresseIp);
    }

    /** §7.2 étape 3 — total par marque, par volume, montant total. */
    @Transactional(readOnly = true)
    public RecapitulatifReception recapitulatif(Long receptionId) {
        Reception reception = trouver(receptionId);
        Map<String, Montant> parMarque = totauxPar(reception, ligne -> ligne.getProduit().getMarque().getLibelle());
        Map<String, Montant> parVolume = totauxPar(reception, ligne -> ligne.getProduit().getVolume().getLibelle());
        return new RecapitulatifReception(reception, parMarque, parVolume, reception.montantTotal());
    }

    private Map<String, Montant> totauxPar(Reception reception, java.util.function.Function<LigneReception, String> cle) {
        return reception.getLignes().stream()
                .collect(Collectors.groupingBy(cle, LinkedHashMap::new,
                        Collectors.reducing(Montant.zero(), LigneReception::montantLigne, Montant::plus)));
    }

    @Transactional(readOnly = true)
    public Reception trouver(Long id) {
        return receptionRepository.parId(id)
                .orElseThrow(() -> new NoSuchElementException("Réception introuvable : " + id));
    }

    @Transactional(readOnly = true)
    public Page<Reception> rechercher(Long pointDeVenteId, StatutReception statut, Pageable pageable) {
        return receptionRepository.rechercher(pointDeVenteId, statut, pageable);
    }

    private Utilisateur utilisateur(Long id) {
        return utilisateurRepository.parId(id)
                .orElseThrow(() -> new NoSuchElementException("Utilisateur introuvable : " + id));
    }

    private static LigneReception ligneDe(Reception reception, Long ligneId) {
        return reception.getLignes().stream()
                .filter(ligne -> ligne.getId().equals(ligneId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Ligne introuvable : " + ligneId));
    }

    private static String decrireLigne(LigneReception ligne) {
        return "nombreCasiers=" + ligne.getNombreCasiers() + ", prixAchatCasierXof=" + ligne.getPrixAchatCasier().valeurXof();
    }
}
