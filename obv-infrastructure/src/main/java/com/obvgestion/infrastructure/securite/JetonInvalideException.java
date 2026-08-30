package com.obvgestion.infrastructure.securite;

import com.obvgestion.domain.commun.RegleGestionException;

public final class JetonInvalideException extends RegleGestionException {
    public JetonInvalideException(String message) {
        super("JETON_INVALIDE", message);
    }
}
