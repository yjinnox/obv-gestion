package com.obvgestion.domain.utilisateur;

import com.obvgestion.domain.commun.RegleGestionException;

/** §4.4 — verrouillage temporaire après 5 échecs de connexion (15 minutes). */
public final class CompteVerrouilleException extends RegleGestionException {
    public CompteVerrouilleException() {
        super("COMPTE_VERROUILLE", "Compte temporairement verrouillé suite à plusieurs échecs. Réessayez plus tard.");
    }
}
