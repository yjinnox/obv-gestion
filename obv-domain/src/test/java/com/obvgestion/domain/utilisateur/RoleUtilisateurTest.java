package com.obvgestion.domain.utilisateur;

import com.obvgestion.domain.referentiel.TypePointDeVente;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static com.obvgestion.domain.utilisateur.Permission.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verrouille la matrice de permissions §3.3, complétée par décision produit
 * pour les colonnes qu'elle ne couvrait pas explicitement (UTILISATEUR_READ,
 * RECEPTION_READ, VENTE_READ, CLIENT_READ, CLIENT_WRITE : réservées aux
 * rôles globaux) et par RG-01 pour TRANSFERT_VALIDER (SUPER_ADMIN seul).
 */
class RoleUtilisateurTest {

    @Test
    void superAdministrateurPossedeToutesLesPermissions() {
        assertThat(RoleUtilisateur.SUPER_ADMINISTRATEUR.permissions())
                .containsExactlyInAnyOrderElementsOf(EnumSet.allOf(Permission.class));
    }

    @Test
    void administrateurNaPasLesPermissionsOperationnelles() {
        assertThat(RoleUtilisateur.ADMINISTRATEUR.permissions())
                .containsExactlyInAnyOrder(
                        REFERENTIEL_READ, REFERENTIEL_WRITE,
                        UTILISATEUR_READ, UTILISATEUR_WRITE,
                        RECEPTION_READ, VENTE_READ,
                        CLIENT_READ, CLIENT_WRITE,
                        RAPPORT_READ)
                .doesNotContain(RECEPTION_WRITE, RECEPTION_VALIDER, VENTE_WRITE,
                        SESSION_CLOTURER, SESSION_VALIDER, TRANSFERT_WRITE, TRANSFERT_VALIDER,
                        MODIFICATION_POST_CLOTURE);
    }

    @Test
    void seulSuperAdministrateurPeutValider() {
        for (RoleUtilisateur role : RoleUtilisateur.values()) {
            if (role == RoleUtilisateur.SUPER_ADMINISTRATEUR) {
                assertThat(role.permissions()).contains(RECEPTION_VALIDER, SESSION_VALIDER, TRANSFERT_VALIDER);
            } else {
                assertThat(role.permissions())
                        .as("le rôle %s ne doit détenir aucune permission de validation (RG-01)", role)
                        .doesNotContain(RECEPTION_VALIDER, SESSION_VALIDER, TRANSFERT_VALIDER);
            }
        }
    }

    @Test
    void gerantDepotEstScopePointDeVenteDeTypeDepot() {
        assertThat(RoleUtilisateur.GERANT_DEPOT.portee()).isEqualTo(RoleUtilisateur.Portee.POINT_DE_VENTE);
        assertThat(RoleUtilisateur.GERANT_DEPOT.typePointDeVenteRequis()).isEqualTo(TypePointDeVente.DEPOT);
        assertThat(RoleUtilisateur.GERANT_DEPOT.permissions())
                .containsExactlyInAnyOrder(REFERENTIEL_READ, RECEPTION_WRITE, VENTE_WRITE,
                        SESSION_CLOTURER, TRANSFERT_WRITE, RAPPORT_READ);
    }

    @Test
    void gerantBarEstScopePointDeVenteDeTypeBar() {
        assertThat(RoleUtilisateur.GERANT_BAR.portee()).isEqualTo(RoleUtilisateur.Portee.POINT_DE_VENTE);
        assertThat(RoleUtilisateur.GERANT_BAR.typePointDeVenteRequis()).isEqualTo(TypePointDeVente.BAR);
        assertThat(RoleUtilisateur.GERANT_BAR.permissions())
                .containsExactlyInAnyOrder(REFERENTIEL_READ, VENTE_WRITE,
                        SESSION_CLOTURER, TRANSFERT_WRITE, RAPPORT_READ);
    }

    @Test
    void vendeurAccepteToutTypeDePointDeVente() {
        assertThat(RoleUtilisateur.VENDEUR.portee()).isEqualTo(RoleUtilisateur.Portee.POINT_DE_VENTE);
        assertThat(RoleUtilisateur.VENDEUR.typePointDeVenteRequis()).isNull();
        assertThat(RoleUtilisateur.VENDEUR.permissions())
                .containsExactlyInAnyOrder(REFERENTIEL_READ, VENTE_WRITE);
    }

    @Test
    void referentielReadEstAccordeATousLesRoles() {
        for (RoleUtilisateur role : RoleUtilisateur.values()) {
            assertThat(role.permissions()).contains(REFERENTIEL_READ);
        }
    }
}
