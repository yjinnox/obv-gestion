package com.obvgestion.domain.transfert;

/**
 * Cycle de vie d'un transfert dépôt → bar (RG-32, identique à la réception) :
 * {@code BROUILLON --clôturer--> EN_ATTENTE_VALIDATION --valider--> VALIDEE}
 * (final), ou {@code EN_ATTENTE_VALIDATION --annuler--> ANNULEE} (final).
 */
public enum StatutTransfert {
    BROUILLON,
    EN_ATTENTE_VALIDATION,
    VALIDEE,
    ANNULEE
}
