package com.obvgestion.domain.utilisateur;

/**
 * Mot de passe en clair, validé selon la politique RG-02, avant hachage.
 * N'est jamais persisté tel quel.
 *
 * <p>RG-02 — Politique de mot de passe : au moins 10 caractères, au moins
 * une minuscule, une majuscule et un chiffre.
 */
public record MotDePasseClair(String valeur) {

    private static final int LONGUEUR_MIN = 10;

    public MotDePasseClair {
        if (estInvalide(valeur)) {
            throw new MotDePasseInvalideException(
                    "Le mot de passe doit comporter au moins " + LONGUEUR_MIN
                            + " caractères, avec au moins une minuscule, une majuscule et un chiffre.");
        }
    }

    private static boolean estInvalide(String valeur) {
        return valeur == null
                || valeur.length() < LONGUEUR_MIN
                || valeur.chars().noneMatch(Character::isLowerCase)
                || valeur.chars().noneMatch(Character::isUpperCase)
                || valeur.chars().noneMatch(Character::isDigit);
    }
}
