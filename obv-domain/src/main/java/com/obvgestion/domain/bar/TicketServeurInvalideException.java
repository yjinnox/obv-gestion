package com.obvgestion.domain.bar;

import com.obvgestion.domain.commun.RegleGestionException;

/** §10 — violation d'une règle du cycle de vie d'un ticket serveur. */
public final class TicketServeurInvalideException extends RegleGestionException {
    public TicketServeurInvalideException(String message) {
        super("TICKET_SERVEUR_INVALIDE", message);
    }
}
