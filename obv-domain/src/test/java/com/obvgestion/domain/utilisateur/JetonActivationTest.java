package com.obvgestion.domain.utilisateur;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** RG « jeton d'activation à usage unique, TTL 72h » (§4.2). */
class JetonActivationTest {

    private final Utilisateur utilisateur =
            Utilisateur.creer("Kouassi", "Awa", CanalContact.EMAIL, "awa@example.com", null);

    @Test
    void estValideAvantExpirationEtAvantUtilisation() {
        Instant creation = Instant.parse("2026-08-30T10:00:00Z");
        JetonActivation jeton = JetonActivation.creer(utilisateur, "hash", creation);

        assertThat(jeton.estValide(creation.plusSeconds(1))).isTrue();
        assertThat(jeton.estValide(creation.plus(JetonActivation.DUREE_VALIDITE).minusSeconds(1))).isTrue();
    }

    @Test
    void estInvalideApresExpiration() {
        Instant creation = Instant.parse("2026-08-30T10:00:00Z");
        JetonActivation jeton = JetonActivation.creer(utilisateur, "hash", creation);

        assertThat(jeton.estValide(creation.plus(JetonActivation.DUREE_VALIDITE).plusSeconds(1))).isFalse();
    }

    @Test
    void estInvalideApresUtilisation() {
        Instant creation = Instant.now();
        JetonActivation jeton = JetonActivation.creer(utilisateur, "hash", creation);

        jeton.marquerUtilise(creation.plusSeconds(10));

        assertThat(jeton.estValide(creation.plusSeconds(20))).isFalse();
    }

    @Test
    void marquerUtiliseUneSecondeFoisEstRejete() {
        Instant creation = Instant.now();
        JetonActivation jeton = JetonActivation.creer(utilisateur, "hash", creation);
        jeton.marquerUtilise(creation.plusSeconds(1));

        assertThatThrownBy(() -> jeton.marquerUtilise(creation.plusSeconds(2)))
                .isInstanceOf(EtatUtilisateurInvalideException.class);
    }
}
