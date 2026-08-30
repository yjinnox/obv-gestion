package com.obvgestion.domain.utilisateur;

import com.obvgestion.domain.commun.RegleGestionException;

public final class EtatUtilisateurInvalideException extends RegleGestionException {
    public EtatUtilisateurInvalideException(String message) {
        super("ETAT_UTILISATEUR_INVALIDE", message);
    }
}
