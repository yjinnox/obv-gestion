package com.obvgestion.domain.vente;

/**
 * Cycle de vie d'une session de vente (§8.1) :
 * {@code OUVERTE --clôturer--> CLOTUREE --valider--> VALIDEE} (final), ou
 * {@code CLOTUREE --demander modification--> EN_MODIFICATION --valider--> VALIDEE}.
 */
public enum StatutSessionVente {
    OUVERTE,
    CLOTUREE,
    EN_MODIFICATION,
    VALIDEE
}
