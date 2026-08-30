package com.obvgestion.domain.utilisateur;

import com.obvgestion.domain.commun.RegleGestionException;

public final class CodeOtpInvalideException extends RegleGestionException {
    public CodeOtpInvalideException(String message) {
        super("OTP_INVALIDE", message);
    }
}
