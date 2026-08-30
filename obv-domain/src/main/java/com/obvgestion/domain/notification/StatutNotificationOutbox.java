package com.obvgestion.domain.notification;

/** Cycle de vie d'une entrée de la file d'envoi (§11, pattern transactional outbox). */
public enum StatutNotificationOutbox {
    EN_ATTENTE,
    ENVOYE,
    ECHEC_DEFINITIF
}
