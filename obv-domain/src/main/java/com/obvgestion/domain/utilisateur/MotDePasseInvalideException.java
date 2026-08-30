package com.obvgestion.domain.utilisateur;

import com.obvgestion.domain.commun.RegleGestionException;

public final class MotDePasseInvalideException extends RegleGestionException {
    public MotDePasseInvalideException(String message) {
        super("MOT_DE_PASSE_INVALIDE", message);
    }
}
