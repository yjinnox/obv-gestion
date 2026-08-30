package com.obvgestion.domain.utilisateur;

import com.obvgestion.domain.referentiel.PointDeVente;
import com.obvgestion.domain.referentiel.TypePointDeVente;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AffectationTest {

    private final Utilisateur utilisateur =
            Utilisateur.creer("Kouassi", "Awa", CanalContact.EMAIL, "awa@example.com", null);

    @Test
    void unRoleGlobalNaAucunPointDeVente() {
        Affectation affectation = Affectation.of(utilisateur, RoleUtilisateur.SUPER_ADMINISTRATEUR, null);
        assertThat(affectation.getPointDeVente()).isNull();
    }

    @Test
    void unRoleGlobalAvecUnPointDeVenteEstRejete() {
        PointDeVente depot = new PointDeVente("Dépôt central", TypePointDeVente.DEPOT, "Abidjan");
        assertThatThrownBy(() -> Affectation.of(utilisateur, RoleUtilisateur.ADMINISTRATEUR, depot))
                .isInstanceOf(AffectationInvalideException.class);
    }

    @Test
    void gerantDepotSansPointDeVenteEstRejete() {
        assertThatThrownBy(() -> Affectation.of(utilisateur, RoleUtilisateur.GERANT_DEPOT, null))
                .isInstanceOf(AffectationInvalideException.class);
    }

    @Test
    void gerantDepotSurUnBarEstRejete() {
        PointDeVente bar = new PointDeVente("Le Maquis", TypePointDeVente.BAR, "Abidjan");
        assertThatThrownBy(() -> Affectation.of(utilisateur, RoleUtilisateur.GERANT_DEPOT, bar))
                .isInstanceOf(AffectationInvalideException.class);
    }

    @Test
    void gerantDepotSurUnDepotEstAccepte() {
        PointDeVente depot = new PointDeVente("Dépôt central", TypePointDeVente.DEPOT, "Abidjan");
        Affectation affectation = Affectation.of(utilisateur, RoleUtilisateur.GERANT_DEPOT, depot);
        assertThat(affectation.getPointDeVente()).isEqualTo(depot);
        assertThat(affectation.getRole()).isEqualTo(RoleUtilisateur.GERANT_DEPOT);
    }

    @Test
    void vendeurAccepteUnBarOuUnDepot() {
        PointDeVente bar = new PointDeVente("Le Maquis", TypePointDeVente.BAR, "Abidjan");
        PointDeVente depot = new PointDeVente("Dépôt central", TypePointDeVente.DEPOT, "Abidjan");
        assertThat(Affectation.of(utilisateur, RoleUtilisateur.VENDEUR, bar).getPointDeVente()).isEqualTo(bar);
        assertThat(Affectation.of(utilisateur, RoleUtilisateur.VENDEUR, depot).getPointDeVente()).isEqualTo(depot);
    }
}
