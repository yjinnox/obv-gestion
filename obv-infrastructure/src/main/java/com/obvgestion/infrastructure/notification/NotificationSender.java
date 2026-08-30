package com.obvgestion.infrastructure.notification;

import com.obvgestion.application.notification.Notification;
import com.obvgestion.domain.notification.CanalNotification;

/**
 * §11 — interface avec deux implémentations réelles ({@code EmailNotificationSender},
 * {@code SmsNotificationSender}) et une implémentation {@code LogNotificationSender}
 * pour le développement (propriété {@code notification.mode}). Usage interne à
 * l'infrastructure : seul {@link NotificationOutboxProcessor} en dépend.
 */
interface NotificationSender {

    boolean supporte(CanalNotification canal);

    void envoyer(Notification notification);
}
