package com.obvgestion.application.notification;

import com.obvgestion.domain.notification.CanalNotification;

import java.util.Map;

/**
 * Notification à envoyer de façon asynchrone (§11). {@code gabarit} désigne
 * le nom du template Thymeleaf (sans extension, ex. {@code "invitation-activation"}),
 * {@code variables} ses variables typées.
 */
public record Notification(CanalNotification canal, String destinataire, String gabarit,
                            Map<String, Object> variables) {

    public Notification {
        variables = Map.copyOf(variables);
    }
}
