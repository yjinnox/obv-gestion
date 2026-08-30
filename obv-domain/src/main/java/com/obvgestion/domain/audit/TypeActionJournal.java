package com.obvgestion.domain.audit;

/**
 * Catégories d'actions sensibles tracées dans {@link JournalAction} (§12).
 * {@code VALIDATION}/{@code ANNULATION}/{@code MODIFICATION_POST_CLOTURE}
 * sont génériques : réutilisées pour les réceptions (P3), sessions de vente
 * (P4/P6) et transferts (P5).
 */
public enum TypeActionJournal {
    COMPTE_ACTIVE,
    COMPTE_DESACTIVE,
    COMPTE_REACTIVE,
    COMPTE_ARCHIVE,
    DROITS_MODIFIES,
    VALIDATION,
    ANNULATION,
    MODIFICATION_POST_CLOTURE
}
