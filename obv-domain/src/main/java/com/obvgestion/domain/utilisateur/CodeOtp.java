package com.obvgestion.domain.utilisateur;

import com.obvgestion.domain.commun.Hachage;

import java.security.SecureRandom;

/**
 * Code à usage unique envoyé lors de l'activation d'un compte (§4.2).
 *
 * <p>RG-03 — Le code comporte exactement 6 chiffres (une correction de la
 * demande initiale, qui proposait 4 chiffres : insuffisant, 10 000
 * combinaisons seulement).
 */
public record CodeOtp(String valeur) {

    private static final int LONGUEUR = 6;
    private static final SecureRandom ALEA = new SecureRandom();

    public CodeOtp {
        if (valeur == null || !valeur.matches("\\d{" + LONGUEUR + "}")) {
            throw new CodeOtpInvalideException("Le code OTP doit comporter exactement " + LONGUEUR + " chiffres.");
        }
    }

    public static CodeOtp genererAleatoire() {
        int nombre = ALEA.nextInt(1_000_000);
        return new CodeOtp("%06d".formatted(nombre));
    }

    public String hacher() {
        return Hachage.sha256Hex(valeur);
    }
}
