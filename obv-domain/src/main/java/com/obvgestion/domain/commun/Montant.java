package com.obvgestion.domain.commun;

/**
 * Montant monétaire en FCFA (XOF), toujours entier (H1 : aucune décimale,
 * jamais de {@code double}/{@code float}).
 */
public record Montant(long valeurXof) {

    public Montant {
        if (valeurXof < 0) {
            throw new IllegalArgumentException("Un montant ne peut pas être négatif : " + valeurXof);
        }
    }

    public static Montant zero() {
        return new Montant(0);
    }

    public Montant plus(Montant autre) {
        return new Montant(this.valeurXof + autre.valeurXof);
    }

    public Montant multiplie(long facteur) {
        return new Montant(this.valeurXof * facteur);
    }

    /** Applique un taux en pourcentage (ex. TVA), arrondi à l'entier le plus proche (H1 : jamais de décimale). */
    public Montant pourcentage(int tauxPourcent) {
        return new Montant(Math.round(this.valeurXof * tauxPourcent / 100.0));
    }
}
