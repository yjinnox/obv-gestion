package com.obvgestion.infrastructure.notification;

import com.obvgestion.application.notification.Notification;
import com.obvgestion.domain.notification.CanalNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Implémentation de développement (§11) : journalise au lieu d'envoyer réellement. */
@Component
@ConditionalOnProperty(prefix = "notification", name = "mode", havingValue = "log")
class LogNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(LogNotificationSender.class);

    @Override
    public boolean supporte(CanalNotification canal) {
        return true;
    }

    @Override
    public void envoyer(Notification notification) {
        log.info("[NOTIFICATION-LOG] canal={} destinataire={} gabarit={} variables={}",
                notification.canal(), notification.destinataire(), notification.gabarit(), notification.variables());
    }
}
