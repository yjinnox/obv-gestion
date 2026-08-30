package com.obvgestion.domain.utilisateur;

/**
 * Cycle de vie d'un compte utilisateur (§4). Un compte archivé ne peut
 * plus se connecter et n'apparaît plus dans les listes de sélection
 * (RG-05) ; aucune transition ne supprime physiquement l'enregistrement.
 */
public enum StatutUtilisateur {
    EN_ATTENTE_ACTIVATION,
    ACTIF,
    DESACTIVE,
    ARCHIVE
}
