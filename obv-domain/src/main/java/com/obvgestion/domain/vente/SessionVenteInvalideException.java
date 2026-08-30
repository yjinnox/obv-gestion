package com.obvgestion.domain.vente;

import com.obvgestion.domain.commun.RegleGestionException;

/** §8 — violation d'une règle du cycle de vie d'une session de vente. */
public final class SessionVenteInvalideException extends RegleGestionException {
    public SessionVenteInvalideException(String message) {
        super("SESSION_VENTE_INVALIDE", message);
    }
}
