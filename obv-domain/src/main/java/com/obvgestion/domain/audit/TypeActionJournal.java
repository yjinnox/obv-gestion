package com.obvgestion.domain.audit;

/**
 * Catégories d'actions sensibles tracées dans {@link JournalAction} (§12).
 * Complétée au fil des phases (validation, annulation, modification
 * post-clôture arrivent avec les documents en P3+).
 */
public enum TypeActionJournal {
    COMPTE_ACTIVE,
    COMPTE_DESACTIVE,
    COMPTE_REACTIVE,
    COMPTE_ARCHIVE,
    DROITS_MODIFIES
}
