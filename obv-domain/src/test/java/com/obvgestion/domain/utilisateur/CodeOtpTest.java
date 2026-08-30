package com.obvgestion.domain.utilisateur;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** RG-03 — le code OTP comporte exactement 6 chiffres. */
class CodeOtpTest {

    @RepeatedTest(20)
    void genereToujoursSixChiffres() {
        CodeOtp code = CodeOtp.genererAleatoire();
        assertThat(code.valeur()).hasSize(6).matches("\\d{6}");
    }

    @Test
    void accepteUnCodeDeSixChiffres() {
        assertThat(new CodeOtp("042518").valeur()).isEqualTo("042518");
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234", "1234567", "abcdef", ""})
    void rejetteUnFormatInvalide(String valeur) {
        assertThatThrownBy(() -> new CodeOtp(valeur)).isInstanceOf(CodeOtpInvalideException.class);
    }

    @Test
    void rejetteUnCodeNul() {
        assertThatThrownBy(() -> new CodeOtp(null)).isInstanceOf(CodeOtpInvalideException.class);
    }
}
