package com.obvgestion.infrastructure.notification.sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Fournisseur SMS de développement : journalise au lieu d'envoyer réellement. */
@Component
@ConditionalOnProperty(prefix = "sms", name = "provider", havingValue = "log", matchIfMissing = true)
class LogSmsProvider implements SmsProvider {

    private static final Logger log = LoggerFactory.getLogger(LogSmsProvider.class);

    @Override
    public void envoyer(String destinataire, String message) {
        log.info("[SMS-LOG] à {} : {}", destinataire, message);
    }
}
