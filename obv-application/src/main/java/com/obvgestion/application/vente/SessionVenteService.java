package com.obvgestion.application.vente;

import com.obvgestion.application.audit.Journalisateur;
import com.obvgestion.application.bar.TicketServeurRepository;
import com.obvgestion.application.notification.Notification;
import com.obvgestion.application.notification.NotificationService;
import com.obvgestion.application.referentiel.PointDeVenteRepository;
import com.obvgestion.application.stock.StockService;
import com.obvgestion.application.utilisateur.UtilisateurRepository;
import com.obvgestion.domain.audit.TypeActionJournal;
import com.obvgestion.domain.bar.StatutTicketServeur;
import com.obvgestion.domain.bar.TicketServeur;
import com.obvgestion.domain.commun.Montant;
import com.obvgestion.domain.commun.SeparationDesTachesException;
import com.obvgestion.domain.referentiel.PointDeVente;
import com.obvgestion.domain.stock.TypeMouvementStock;
import com.obvgestion.domain.utilisateur.Permission;
import com.obvgestion.domain.utilisateur.RoleUtilisateur;
import com.obvgestion.domain.utilisateur.StatutUtilisateur;
import com.obvgestion.domain.utilisateur.Utilisateur;
import com.obvgestion.domain.vente.LigneVente;
import com.obvgestion.domain.vente.SessionVente;
import com.obvgestion.domain.vente.SessionVenteInvalideException;
import com.obvgestion.domain.vente.Vente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;

/** §8.1, §8.3 — cycle de vie de la session de vente (RG-01, RG-23, RG-28, RG-29). */
@Service
public class SessionVenteService {

    private final SessionVenteRepository sessionVenteRepository;
    private final VenteRepository venteRepository;
    private final TicketServeurRepository ticketServeurRepository;
    private final PointDeVenteRepository pointDeVenteRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final StockService stockService;
    private final Journalisateur journalisateur;
    private final NotificationService notificationService;

    public SessionVenteService(SessionVenteRepository sessionVenteRepository, VenteRepository venteRepository,
                                TicketServeurRepository ticketServeurRepository,
                                PointDeVenteRepository pointDeVenteRepository,
                                UtilisateurRepository utilisateurRepository, StockService stockService,
                                Journalisateur journalisateur, NotificationService notificationService) {
        this.sessionVenteRepository = sessionVenteRepository;
        this.venteRepository = venteRepository;
        this.ticketServeurRepository = ticketServeurRepository;
        this.pointDeVenteRepository = pointDeVenteRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.stockService = stockService;
        this.journalisateur = journalisateur;
        this.notificationService = notificationService;
    }

    /** RG-23 — une seule session ouverte par point de vente à la fois. */
    @Transactional
    public SessionVente ouvrir(Long pointDeVenteId, Long acteurId, Montant fondCaisse) {
        PointDeVente pointDeVente = pointDeVenteRepository.parId(pointDeVenteId)
                .orElseThrow(() -> new NoSuchElementException("Point de vente introuvable : " + pointDeVenteId));
        if (sessionVenteRepository.sessionOuverte(pointDeVenteId).isPresent()) {
            throw new SessionVenteInvalideException(
                    "Une session est déjà ouverte pour ce point de vente.");
        }
        return sessionVenteRepository.enregistrer(
                new SessionVente(pointDeVente, acteurId.toString(), fondCaisse, Instant.now()));
    }

    @Transactional(readOnly = true)
    public SessionVente courante(Long pointDeVenteId) {
        return sessionVenteRepository.sessionOuverte(pointDeVenteId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Aucune session ouverte pour le point de vente " + pointDeVenteId));
    }

    /**
     * §8.3/RG-34 — le total théorique est la somme des ventes (dépôt) ou des
     * tickets encaissés (bar) enregistrés dans la session ; une session ne
     * porte jamais les deux à la fois, l'une des deux sommes est donc
     * toujours nulle.
     */
    @Transactional
    public SessionVente cloturer(Long sessionId, Long acteurId, Montant totalCompte) {
        SessionVente session = trouver(sessionId);
        Montant totalVentes = venteRepository.parSession(sessionId).stream()
                .map(Vente::getMontantTotal)
                .reduce(Montant.zero(), Montant::plus);
        Montant totalTickets = ticketServeurRepository.parSession(sessionId).stream()
                .filter(t -> t.getStatut() == StatutTicketServeur.ENCAISSE)
                .map(TicketServeur::getMontantTotal)
                .reduce(Montant.zero(), Montant::plus);
        session.cloturer(acteurId.toString(), totalVentes.plus(totalTickets), totalCompte, Instant.now());
        return sessionVenteRepository.enregistrer(session);
    }

    /** RG-01/RG-28 — un utilisateur ne peut jamais valider une session qu'il a lui-même clôturée. */
    @Transactional
    public void valider(Long sessionId, Long acteurId, String adresseIp) {
        SessionVente session = trouver(sessionId);
        session.valider(acteurId.toString(), Instant.now());
        sessionVenteRepository.enregistrer(session);
        journalisateur.journaliser(acteurId.toString(), TypeActionJournal.VALIDATION, "SessionVente",
                sessionId.toString(), session.getClotureePar(), acteurId.toString(), adresseIp);
    }

    /** RG-29 — notifie l'ensemble des SUPER_ADMINISTRATEUR actifs. */
    @Transactional
    public void demanderModification(Long sessionId) {
        SessionVente session = trouver(sessionId);
        session.demanderModification();
        sessionVenteRepository.enregistrer(session);

        List<Utilisateur> superAdmins = utilisateurRepository
                .rechercher(StatutUtilisateur.ACTIF, RoleUtilisateur.SUPER_ADMINISTRATEUR, null, null,
                        Pageable.unpaged())
                .getContent();
        for (Utilisateur superAdmin : superAdmins) {
            notificationService.envoyer(new Notification(
                    superAdmin.canalNotification(), superAdmin.contactNotification(), "session-vente-demande-modification",
                    Map.of("nom", superAdmin.getNom(), "prenoms", superAdmin.getPrenoms(),
                            "pointDeVente", session.getPointDeVente().getLibelle())));
        }
    }

    /**
     * RG-29 — correction d'une quantité vendue par le SUPER_ADMINISTRATEUR
     * pendant la modification : ajuste le stock de l'écart et journalise.
     */
    @Transactional
    public void modifierQuantiteVente(Long venteId, Long ligneId, long quantiteDemiCasiers, int tauxTvaPourcent,
                                       Long acteurId, String adresseIp) {
        Vente vente = venteRepository.parId(venteId)
                .orElseThrow(() -> new NoSuchElementException("Vente introuvable : " + venteId));
        Utilisateur acteur = utilisateur(acteurId);
        if (!acteur.permissions().contains(Permission.MODIFICATION_POST_CLOTURE)) {
            throw new SeparationDesTachesException(
                    "Seul un SUPER_ADMINISTRATEUR peut modifier une vente pendant la modification de session.");
        }
        var ligne = vente.getLignes().stream().filter(l -> l.getId().equals(ligneId)).findFirst()
                .orElseThrow(() -> new NoSuchElementException("Ligne introuvable : " + ligneId));
        long avant = ligne.quantiteDemiCasiers();
        String descriptionAvant = "quantiteDemiCasiers=" + avant;

        vente.modifierQuantiteLigne(ligneId, quantiteDemiCasiers, tauxTvaPourcent);
        venteRepository.enregistrerEtValider(vente);

        long delta = quantiteDemiCasiers - avant;
        if (delta != 0) {
            stockService.appliquer(vente.getSessionVente().getPointDeVente(), ligne.getProduit(),
                    TypeMouvementStock.AJUSTEMENT, -delta, "VENTE", vente.getId(), acteur);
        }
        journalisateur.journaliser(acteurId.toString(), TypeActionJournal.MODIFICATION_POST_CLOTURE, "LigneVente",
                ligneId.toString(), descriptionAvant, "quantiteDemiCasiers=" + quantiteDemiCasiers, adresseIp);
    }

    /** §8.3 — quantités vendues par marque/volume, recette par mode de paiement. */
    @Transactional(readOnly = true)
    public RecapitulatifSessionVente recapitulatif(Long sessionId) {
        SessionVente session = trouver(sessionId);
        List<Vente> ventes = venteRepository.parSession(sessionId);
        List<LigneVente> lignes = ventes.stream().flatMap(v -> v.getLignes().stream()).toList();

        Map<String, Long> parMarque = totauxParDemiCasiers(lignes, l -> l.getProduit().getMarque().getLibelle());
        Map<String, Long> parVolume = totauxParDemiCasiers(lignes, l -> l.getProduit().getVolume().getLibelle());
        long quantiteTotale = lignes.stream().mapToLong(LigneVente::quantiteDemiCasiers).sum();

        Map<String, Long> parModePaiement = ventes.stream()
                .collect(Collectors.groupingBy(v -> v.getModePaiement().name(), LinkedHashMap::new,
                        Collectors.summingLong(v -> v.getMontantTotal().valeurXof())));
        long recetteTotale = ventes.stream().mapToLong(v -> v.getMontantTotal().valeurXof()).sum();

        return new RecapitulatifSessionVente(session, parMarque, parVolume, quantiteTotale, parModePaiement,
                recetteTotale);
    }

    private static Map<String, Long> totauxParDemiCasiers(List<LigneVente> lignes, Function<LigneVente, String> cle) {
        return lignes.stream().collect(Collectors.groupingBy(cle, LinkedHashMap::new,
                Collectors.summingLong(LigneVente::quantiteDemiCasiers)));
    }

    @Transactional(readOnly = true)
    public SessionVente trouver(Long id) {
        return sessionVenteRepository.parId(id)
                .orElseThrow(() -> new NoSuchElementException("Session de vente introuvable : " + id));
    }

    @Transactional(readOnly = true)
    public Page<SessionVente> rechercher(Long pointDeVenteId, Pageable pageable) {
        return sessionVenteRepository.rechercher(pointDeVenteId, pageable);
    }

    private Utilisateur utilisateur(Long id) {
        return utilisateurRepository.parId(id)
                .orElseThrow(() -> new NoSuchElementException("Utilisateur introuvable : " + id));
    }
}
