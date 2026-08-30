package com.obvgestion.application.utilisateur;

import com.obvgestion.application.audit.Journalisateur;
import com.obvgestion.application.referentiel.PointDeVenteRepository;
import com.obvgestion.domain.audit.TypeActionJournal;
import com.obvgestion.domain.referentiel.PointDeVente;
import com.obvgestion.domain.utilisateur.Affectation;
import com.obvgestion.domain.utilisateur.AutoModificationInterditeException;
import com.obvgestion.domain.utilisateur.DernierSuperAdministrateurException;
import com.obvgestion.domain.utilisateur.RoleUtilisateur;
import com.obvgestion.domain.utilisateur.StatutUtilisateur;
import com.obvgestion.domain.utilisateur.Utilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.NoSuchElementException;

/** §4.1, §4.3 — création et administration des comptes utilisateurs. */
@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final PointDeVenteRepository pointDeVenteRepository;
    private final ActivationService activationService;
    private final Journalisateur journalisateur;

    public UtilisateurService(UtilisateurRepository utilisateurRepository,
                               PointDeVenteRepository pointDeVenteRepository,
                               ActivationService activationService,
                               Journalisateur journalisateur) {
        this.utilisateurRepository = utilisateurRepository;
        this.pointDeVenteRepository = pointDeVenteRepository;
        this.activationService = activationService;
        this.journalisateur = journalisateur;
    }

    /** §4.1 — création d'un compte ; envoie immédiatement l'invitation d'activation (§4.2). */
    @Transactional
    public Utilisateur creer(CreationUtilisateurCommande commande) {
        Utilisateur utilisateur = Utilisateur.creer(
                commande.nom(), commande.prenoms(), commande.canalContact(), commande.email(), commande.telephone());

        for (CreationUtilisateurCommande.AffectationCommande affectationCommande : commande.affectations()) {
            utilisateur.ajouterAffectation(construireAffectation(utilisateur, affectationCommande));
        }

        utilisateur = utilisateurRepository.enregistrer(utilisateur);
        activationService.inviter(utilisateur);
        return utilisateur;
    }

    @Transactional
    public Utilisateur renommer(Long cibleId, String nom, String prenoms) {
        Utilisateur utilisateur = trouver(cibleId);
        utilisateur.renommer(nom, prenoms);
        return utilisateurRepository.enregistrer(utilisateur);
    }

    @Transactional(readOnly = true)
    public Utilisateur trouverParId(Long id) {
        return trouver(id);
    }

    @Transactional(readOnly = true)
    public Page<Utilisateur> rechercher(StatutUtilisateur statut, RoleUtilisateur role, Long pointDeVenteId,
                                         String recherche, Pageable pageable) {
        return utilisateurRepository.rechercher(statut, role, pointDeVenteId, recherche, pageable);
    }

    /** §4.3 — désactivation réversible (RG-06 : jamais son propre compte, jamais le dernier SUPER_ADMIN actif). */
    @Transactional
    public void desactiver(Long cibleId, Long acteurId, String acteurIdentifiant, String adresseIp) {
        interdireAutoModification(cibleId, acteurId, "se désactiver lui-même");
        Utilisateur utilisateur = trouver(cibleId);
        interdireSiDernierSuperAdministrateurActif(utilisateur);

        String avant = utilisateur.getStatut().name();
        utilisateur.desactiver();
        utilisateurRepository.enregistrer(utilisateur);
        journalisateur.journaliser(acteurIdentifiant, TypeActionJournal.COMPTE_DESACTIVE, "Utilisateur",
                cibleId.toString(), avant, utilisateur.getStatut().name(), adresseIp);
    }

    /** §4.3 — réactivation d'un compte désactivé. */
    @Transactional
    public void reactiver(Long cibleId, String acteurIdentifiant, String adresseIp) {
        Utilisateur utilisateur = trouver(cibleId);
        String avant = utilisateur.getStatut().name();
        utilisateur.reactiver();
        utilisateurRepository.enregistrer(utilisateur);
        journalisateur.journaliser(acteurIdentifiant, TypeActionJournal.COMPTE_REACTIVE, "Utilisateur",
                cibleId.toString(), avant, utilisateur.getStatut().name(), adresseIp);
    }

    /** RG-05 — archivage définitif, jamais de suppression physique. */
    @Transactional
    public void archiver(Long cibleId, Long acteurId, String acteurIdentifiant, String adresseIp) {
        interdireAutoModification(cibleId, acteurId, "s'archiver lui-même");
        Utilisateur utilisateur = trouver(cibleId);
        interdireSiDernierSuperAdministrateurActif(utilisateur);

        String avant = utilisateur.getStatut().name();
        utilisateur.archiver(Instant.now());
        utilisateurRepository.enregistrer(utilisateur);
        journalisateur.journaliser(acteurIdentifiant, TypeActionJournal.COMPTE_ARCHIVE, "Utilisateur",
                cibleId.toString(), avant, utilisateur.getStatut().name(), adresseIp);
    }

    /** §4.3 — ajout d'un rôle/point de vente. */
    @Transactional
    public void ajouterAffectation(Long cibleId, RoleUtilisateur role, Long pointDeVenteId,
                                    String acteurIdentifiant, String adresseIp) {
        Utilisateur utilisateur = trouver(cibleId);
        utilisateur.ajouterAffectation(construireAffectation(
                utilisateur, new CreationUtilisateurCommande.AffectationCommande(role, pointDeVenteId)));
        utilisateurRepository.enregistrer(utilisateur);
        journalisateur.journaliser(acteurIdentifiant, TypeActionJournal.DROITS_MODIFIES, "Utilisateur",
                cibleId.toString(), null, role.name(), adresseIp);
    }

    /** RG-06 — un utilisateur ne peut pas se retirer son propre rôle. */
    @Transactional
    public void retirerAffectation(Long cibleId, Long affectationId, Long acteurId, String acteurIdentifiant,
                                    String adresseIp) {
        interdireAutoModification(cibleId, acteurId, "modifier ses propres droits");
        Utilisateur utilisateur = trouver(cibleId);
        Affectation affectation = utilisateur.getAffectations().stream()
                .filter(a -> a.getId().equals(affectationId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Affectation introuvable : " + affectationId));

        if (affectation.getRole() == RoleUtilisateur.SUPER_ADMINISTRATEUR
                && utilisateur.getStatut() == StatutUtilisateur.ACTIF) {
            interdireSiDernierSuperAdministrateurActif(utilisateur);
        }

        utilisateur.retirerAffectation(affectation);
        utilisateurRepository.enregistrer(utilisateur);
        journalisateur.journaliser(acteurIdentifiant, TypeActionJournal.DROITS_MODIFIES, "Utilisateur",
                cibleId.toString(), affectation.getRole().name(), null, adresseIp);
    }

    /** §4.3 — réinitialisation du mot de passe : renvoie le compte par le parcours d'activation (§4.2). */
    @Transactional
    public void reinitialiserMotDePasse(Long cibleId) {
        Utilisateur utilisateur = trouver(cibleId);
        utilisateur.reinitialiserPourNouvelleActivation();
        utilisateurRepository.enregistrer(utilisateur);
        activationService.inviter(utilisateur);
    }

    private Affectation construireAffectation(Utilisateur utilisateur,
                                               CreationUtilisateurCommande.AffectationCommande commande) {
        PointDeVente pointDeVente = commande.pointDeVenteId() == null ? null
                : pointDeVenteRepository.parId(commande.pointDeVenteId())
                        .orElseThrow(() -> new NoSuchElementException(
                                "Point de vente introuvable : " + commande.pointDeVenteId()));
        return Affectation.of(utilisateur, commande.role(), pointDeVente);
    }

    private Utilisateur trouver(Long id) {
        return utilisateurRepository.parId(id)
                .orElseThrow(() -> new NoSuchElementException("Utilisateur introuvable : " + id));
    }

    private static void interdireAutoModification(Long cibleId, Long acteurId, String action) {
        if (cibleId.equals(acteurId)) {
            throw new AutoModificationInterditeException("Un utilisateur ne peut pas " + action + ".");
        }
    }

    private void interdireSiDernierSuperAdministrateurActif(Utilisateur utilisateur) {
        boolean estSuperAdminActif = utilisateur.getStatut() == StatutUtilisateur.ACTIF
                && utilisateur.possedeRole(RoleUtilisateur.SUPER_ADMINISTRATEUR);
        if (estSuperAdminActif
                && utilisateurRepository.compterParStatutEtRole(
                        StatutUtilisateur.ACTIF, RoleUtilisateur.SUPER_ADMINISTRATEUR) <= 1) {
            throw new DernierSuperAdministrateurException();
        }
    }
}
