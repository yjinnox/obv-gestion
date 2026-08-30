package com.obvgestion.domain.commun;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** H1 — montants FCFA entiers, jamais de décimale. */
class MontantTest {

    @Test
    void rejetteUnMontantNegatif() {
        assertThatThrownBy(() -> new Montant(-1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void additionneDeuxMontants() {
        assertThat(new Montant(1500).plus(new Montant(500))).isEqualTo(new Montant(2000));
    }

    @Test
    void multiplieParUnFacteur() {
        assertThat(new Montant(1500).multiplie(3)).isEqualTo(new Montant(4500));
    }
}
