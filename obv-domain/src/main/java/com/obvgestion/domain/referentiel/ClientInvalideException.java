package com.obvgestion.domain.referentiel;

import com.obvgestion.domain.commun.RegleGestionException;

/** RG-07 — champs obligatoires/interdits selon le type de client. */
public final class ClientInvalideException extends RegleGestionException {
    public ClientInvalideException(String message) {
        super("CLIENT_INVALIDE", message);
    }
}
