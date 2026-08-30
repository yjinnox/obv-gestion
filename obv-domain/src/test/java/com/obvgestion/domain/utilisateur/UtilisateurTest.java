package com.obvgestion.domain.utilisateur;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UtilisateurTest {

    @Test
    void creerExigeUnEmailValidePourLeCanalEmail() {
        assertThatThrownBy(() -> Utilisateur.creer("Kouassi", "Awa", CanalContact.EMAIL, "pas-un-email", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void creerExigeUnTelephoneE164PourLeCanalTelephone() {
        assertThatThrownBy(() -> Utilisateur.creer("Kouassi", "Awa", CanalContact.TELEPHONE, null, "0700000000"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void creerAcceptePersistantLesDonneesValides() {
        Utilisateur utilisateur = Utilisateur.creer(
                "Kouassi", "Awa", CanalContact.TELEPHONE, null, "+2250700000000");
        assertThat(utilisateur.getStatut()).isEqualTo(StatutUtilisateur.EN_ATTENTE_ACTIVATION);
        assertThat(utilisateur.getTelephone()).isEqualTo("+2250700000000");
    }

    @Test
    void parcoursActivationNominal() {
        Utilisateur utilisateur = Utilisateur.creer(
                "Kouassi", "Awa", CanalContact.EMAIL, "awa@example.com", null);

        utilisateur.definirMotDePasse("hash-argon2id");
        assertThat(utilisateur.getStatut()).isEqualTo(StatutUtilisateur.EN_ATTENTE_ACTIVATION);

        utilisateur.activer();
        assertThat(utilisateur.getStatut()).isEqualTo(StatutUtilisateur.ACTIF);
    }

    @Test
    void activerSansMotDePasseEstRejete() {
        Utilisateur utilisateur = Utilisateur.creer(
                "Kouassi", "Awa", CanalContact.EMAIL, "awa@example.com", null);
        assertThatThrownBy(utilisateur::activer).isInstanceOf(EtatUtilisateurInvalideException.class);
    }

    @Test
    void activerUnCompteDejaActifEstRejete() {
        Utilisateur utilisateur = utilisateurActif();
        assertThatThrownBy(utilisateur::activer).isInstanceOf(EtatUtilisateurInvalideException.class);
    }

    @Test
    void desactiverPuisReactiver() {
        Utilisateur utilisateur = utilisateurActif();

        utilisateur.desactiver();
        assertThat(utilisateur.getStatut()).isEqualTo(StatutUtilisateur.DESACTIVE);

        utilisateur.reactiver();
        assertThat(utilisateur.getStatut()).isEqualTo(StatutUtilisateur.ACTIF);
    }

    @Test
    void desactiverUnCompteNonActifEstRejete() {
        Utilisateur utilisateur = Utilisateur.creer(
                "Kouassi", "Awa", CanalContact.EMAIL, "awa@example.com", null);
        assertThatThrownBy(utilisateur::desactiver).isInstanceOf(EtatUtilisateurInvalideException.class);
    }

    /** RG-05 — l'archivage ne supprime rien physiquement, il fige un statut terminal horodaté. */
    @Test
    void archiverFigeLeStatutEtLaDate() {
        Utilisateur utilisateur = utilisateurActif();
        Instant maintenant = Instant.parse("2026-08-30T10:00:00Z");

        utilisateur.archiver(maintenant);

        assertThat(utilisateur.getStatut()).isEqualTo(StatutUtilisateur.ARCHIVE);
        assertThat(utilisateur.getDateArchivage()).isEqualTo(maintenant);
    }

    @Test
    void archiverDeuxFoisEstRejete() {
        Utilisateur utilisateur = utilisateurActif();
        Instant maintenant = Instant.now();
        utilisateur.archiver(maintenant);

        assertThatThrownBy(() -> utilisateur.archiver(maintenant))
                .isInstanceOf(EtatUtilisateurInvalideException.class);
    }

    @Test
    void permissionsAgregeLesRolesDeToutesLesAffectations() {
        Utilisateur utilisateur = utilisateurActif();
        utilisateur.ajouterAffectation(Affectation.of(utilisateur, RoleUtilisateur.SUPER_ADMINISTRATEUR, null));

        assertThat(utilisateur.possedeRole(RoleUtilisateur.SUPER_ADMINISTRATEUR)).isTrue();
        assertThat(utilisateur.permissions()).contains(Permission.RECEPTION_VALIDER, Permission.REFERENTIEL_READ);
    }

    private static Utilisateur utilisateurActif() {
        Utilisateur utilisateur = Utilisateur.creer(
                "Kouassi", "Awa", CanalContact.EMAIL, "awa@example.com", null);
        utilisateur.definirMotDePasse("hash-argon2id");
        utilisateur.activer();
        return utilisateur;
    }
}
