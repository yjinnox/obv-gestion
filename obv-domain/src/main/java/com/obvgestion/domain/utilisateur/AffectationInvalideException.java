package com.obvgestion.domain.utilisateur;

import com.obvgestion.domain.commun.RegleGestionException;

public final class AffectationInvalideException extends RegleGestionException {
    public AffectationInvalideException(String message) {
        super("AFFECTATION_INVALIDE", message);
    }
}
