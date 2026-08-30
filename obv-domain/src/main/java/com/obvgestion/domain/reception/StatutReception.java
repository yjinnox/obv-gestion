package com.obvgestion.domain.reception;

/**
 * Cycle de vie d'une réception (§7.1) :
 * {@code BROUILLON --clôturer--> EN_ATTENTE_VALIDATION --valider--> VALIDEE}
 * (final), ou {@code EN_ATTENTE_VALIDATION --annuler--> ANNULEE} (final).
 */
public enum StatutReception {
    BROUILLON,
    EN_ATTENTE_VALIDATION,
    VALIDEE,
    ANNULEE
}
