package com.obvgestion.domain.vente;

import com.obvgestion.domain.commun.RegleGestionException;

/** §8.2 — violation d'une règle de constitution ou de commande d'une vente. */
public final class VenteInvalideException extends RegleGestionException {
    public VenteInvalideException(String message) {
        super("VENTE_INVALIDE", message);
    }
}
