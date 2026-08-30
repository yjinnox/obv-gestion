package com.obvgestion.domain.commun;

/**
 * RG-01 — la validation définitive d'une réception, d'un transfert ou d'une
 * session de vente est réservée au SUPER_ADMINISTRATEUR, et un utilisateur
 * ne peut jamais valider un document qu'il a lui-même clôturé.
 */
public final class SeparationDesTachesException extends RegleGestionException {
    public SeparationDesTachesException(String message) {
        super("SEPARATION_DES_TACHES", message);
    }
}
