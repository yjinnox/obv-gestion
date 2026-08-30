package com.obvgestion.domain.reception;

import com.obvgestion.domain.commun.RegleGestionException;

/** §7 — violation d'une règle du cycle de vie d'une réception. */
public final class ReceptionInvalideException extends RegleGestionException {
    public ReceptionInvalideException(String message) {
        super("RECEPTION_INVALIDE", message);
    }
}
