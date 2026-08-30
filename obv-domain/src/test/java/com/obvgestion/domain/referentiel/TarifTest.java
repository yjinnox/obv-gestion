package com.obvgestion.domain.referentiel;

import com.obvgestion.domain.commun.Montant;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TarifTest {

    private final PointDeVente depot = new PointDeVente("Dépôt central", TypePointDeVente.DEPOT, "Abidjan");
    private final PointDeVente bar = new PointDeVente("Le Maquis", TypePointDeVente.BAR, "Abidjan");
    private final Produit produit = new Produit(new Marque("Flag"), new Volume("33 cl", 330));

    /** RG-10 — un DEPOT n'utilise que CASIER. */
    @Test
    void depotAvecUniteBouteilleEstRejete() {
        assertThatThrownBy(() -> Tarif.creer(depot, produit, UniteVente.BOUTEILLE, NatureTarif.VENTE,
                new Montant(9000), LocalDate.of(2026, 1, 1)))
                .isInstanceOf(TarifInvalideException.class);
    }

    /** RG-10 — un BAR n'utilise que BOUTEILLE. */
    @Test
    void barAvecUniteCasierEstRejete() {
        assertThatThrownBy(() -> Tarif.creer(bar, produit, UniteVente.CASIER, NatureTarif.VENTE,
                new Montant(9000), LocalDate.of(2026, 1, 1)))
                .isInstanceOf(TarifInvalideException.class);
    }

    @Test
    void depotAvecUniteCasierEstAccepte() {
        Tarif tarif = Tarif.creer(depot, produit, UniteVente.CASIER, NatureTarif.ACHAT,
                new Montant(8500), LocalDate.of(2026, 1, 1));
        assertThat(tarif.getMontant()).isEqualTo(new Montant(8500));
    }

    /** RG-08 — un tarif clos n'est plus actif à partir de sa date de fin (borne exclusive). */
    @Test
    void unTarifNestPlusActifApresSaCloture() {
        Tarif tarif = Tarif.creer(depot, produit, UniteVente.CASIER, NatureTarif.VENTE,
                new Montant(9000), LocalDate.of(2026, 1, 1));
        assertThat(tarif.estActif(LocalDate.of(2026, 3, 1))).isTrue();

        tarif.cloturer(LocalDate.of(2026, 3, 1));

        assertThat(tarif.estActif(LocalDate.of(2026, 2, 28))).isTrue();
        assertThat(tarif.estActif(LocalDate.of(2026, 3, 1))).isFalse();
        assertThat(tarif.estActif(LocalDate.of(2026, 4, 1))).isFalse();
    }

    @Test
    void unTarifDejaClosNePeutPasEtreReClos() {
        Tarif tarif = Tarif.creer(depot, produit, UniteVente.CASIER, NatureTarif.VENTE,
                new Montant(9000), LocalDate.of(2026, 1, 1));
        tarif.cloturer(LocalDate.of(2026, 3, 1));

        assertThatThrownBy(() -> tarif.cloturer(LocalDate.of(2026, 4, 1)))
                .isInstanceOf(TarifInvalideException.class);
    }

    @Test
    void laDateDeClotureNePeutPasPrecederLaDateDeDebut() {
        Tarif tarif = Tarif.creer(depot, produit, UniteVente.CASIER, NatureTarif.VENTE,
                new Montant(9000), LocalDate.of(2026, 3, 1));

        assertThatThrownBy(() -> tarif.cloturer(LocalDate.of(2026, 1, 1)))
                .isInstanceOf(TarifInvalideException.class);
    }

    @Test
    void unTarifSansDateDeFinResteActifIndefiniment() {
        Tarif tarif = Tarif.creer(depot, produit, UniteVente.CASIER, NatureTarif.VENTE,
                new Montant(9000), LocalDate.of(2026, 1, 1));
        assertThat(tarif.estActif(LocalDate.of(2099, 1, 1))).isTrue();
        assertThat(tarif.getDateFin()).isNull();
    }
}
