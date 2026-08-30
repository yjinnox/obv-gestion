package com.obvgestion.domain.utilisateur;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** RG-02 — politique de mot de passe. */
class MotDePasseClairTest {

    @Test
    void accepteUnMotDePasseConforme() {
        assertThat(new MotDePasseClair("Abcdefgh12").valeur()).isEqualTo("Abcdefgh12");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Abcdef12",       // trop court (8 < 10)
            "abcdefghij1",    // pas de majuscule
            "ABCDEFGHIJ1",    // pas de minuscule
            "Abcdefghij",     // pas de chiffre
    })
    void rejetteUnMotDePasseNonConforme(String motDePasse) {
        assertThatThrownBy(() -> new MotDePasseClair(motDePasse))
                .isInstanceOf(MotDePasseInvalideException.class);
    }

    @Test
    void rejetteUnMotDePasseNul() {
        assertThatThrownBy(() -> new MotDePasseClair(null))
                .isInstanceOf(MotDePasseInvalideException.class);
    }
}
