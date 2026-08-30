package com.obvgestion.domain.utilisateur;

/**
 * Permissions granulaires du système (§3.2). Cumulables via les rôles
 * qu'un utilisateur détient.
 */
public enum Permission {
    REFERENTIEL_READ,
    REFERENTIEL_WRITE,
    UTILISATEUR_READ,
    UTILISATEUR_WRITE,
    RECEPTION_READ,
    RECEPTION_WRITE,
    RECEPTION_VALIDER,
    VENTE_READ,
    VENTE_WRITE,
    SESSION_CLOTURER,
    SESSION_VALIDER,
    TRANSFERT_WRITE,
    TRANSFERT_VALIDER,
    CLIENT_READ,
    CLIENT_WRITE,
    RAPPORT_READ,
    MODIFICATION_POST_CLOTURE
}
