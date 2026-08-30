package com.obvgestion.domain.commun;

/**
 * Base des exceptions représentant la violation d'une règle de gestion.
 * Porte un {@code code} métier stable exploité par la couche API (RFC 7807).
 */
public abstract class RegleGestionException extends RuntimeException {

    private final String code;

    protected RegleGestionException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
