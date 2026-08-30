package com.obvgestion.domain.utilisateur;

import com.obvgestion.domain.commun.RegleGestionException;

/** RG-06 — il doit toujours rester au moins un SUPER_ADMINISTRATEUR actif. */
public final class DernierSuperAdministrateurException extends RegleGestionException {
    public DernierSuperAdministrateurException() {
        super("DERNIER_SUPER_ADMINISTRATEUR",
                "Impossible : il doit toujours rester au moins un SUPER_ADMINISTRATEUR actif.");
    }
}
