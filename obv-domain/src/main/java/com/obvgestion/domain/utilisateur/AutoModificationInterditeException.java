package com.obvgestion.domain.utilisateur;

import com.obvgestion.domain.commun.RegleGestionException;

/** RG-06 — un utilisateur ne peut ni s'auto-désactiver, ni se retirer son propre rôle. */
public final class AutoModificationInterditeException extends RegleGestionException {
    public AutoModificationInterditeException(String message) {
        super("AUTO_MODIFICATION_INTERDITE", message);
    }
}
