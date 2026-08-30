package com.obvgestion.domain.bar;

/**
 * Cycle de vie d'un ticket serveur (§10) : {@code OUVERT} pendant que le
 * serveur passe commande auprès du gérant, {@code ENCAISSE} une fois le
 * paiement collecté et les bouteilles remises (final).
 */
public enum StatutTicketServeur {
    OUVERT,
    ENCAISSE
}
