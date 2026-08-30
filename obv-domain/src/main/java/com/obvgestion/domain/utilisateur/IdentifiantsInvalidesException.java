package com.obvgestion.domain.utilisateur;

import com.obvgestion.domain.commun.RegleGestionException;

/** §4.4 — message générique unique (identifiant inconnu ou mot de passe incorrect). */
public final class IdentifiantsInvalidesException extends RegleGestionException {
    public IdentifiantsInvalidesException() {
        super("IDENTIFIANTS_INVALIDES", "Identifiant ou mot de passe incorrect.");
    }
}
