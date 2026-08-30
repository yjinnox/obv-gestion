package com.obvgestion.domain.transfert;

import com.obvgestion.domain.commun.RegleGestionException;

/** §9 — violation d'une règle du cycle de vie d'un transfert dépôt → bar. */
public final class TransfertInvalideException extends RegleGestionException {
    public TransfertInvalideException(String message) {
        super("TRANSFERT_INVALIDE", message);
    }
}
