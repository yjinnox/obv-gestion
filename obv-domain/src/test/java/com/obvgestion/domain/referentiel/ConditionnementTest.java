package com.obvgestion.domain.referentiel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConditionnementTest {

    private final Produit produit = new Produit(new Marque("Flag"), new Volume("33 cl", 330));

    @ParameterizedTest
    @CsvSource({"12,true", "16,true", "24,true", "9,false", "15,false"})
    void demiCasierAutoriseUniquementSiCapacitePaire(int capacite, boolean attendu) {
        Conditionnement conditionnement = new Conditionnement(produit, capacite);
        assertThat(conditionnement.isDemiCasierAutorise()).isEqualTo(attendu);
    }

    @Test
    void rejetteUneCapaciteNulleOuNegative() {
        assertThatThrownBy(() -> new Conditionnement(produit, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
