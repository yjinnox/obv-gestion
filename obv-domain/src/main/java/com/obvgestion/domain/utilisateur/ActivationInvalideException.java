package com.obvgestion.domain.utilisateur;

import com.obvgestion.domain.commun.RegleGestionException;

/**
 * RG-04 — message générique unique pour un jeton d'activation ou un OTP
 * consommé, expiré, épuisé ou inconnu (pas d'énumération de comptes).
 */
public final class ActivationInvalideException extends RegleGestionException {
    public ActivationInvalideException() {
        super("ACTIVATION_INVALIDE", "Ce lien ou ce code n'est plus valide.");
    }
}
