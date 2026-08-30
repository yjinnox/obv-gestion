package com.obvgestion.infrastructure.notification;

import com.obvgestion.application.notification.Notification;
import com.obvgestion.application.notification.NotificationService;
import com.obvgestion.domain.notification.NotificationOutbox;
import com.obvgestion.infrastructure.persistence.NotificationOutboxRepository;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * §11 — écrit la notification dans la file (même transaction que
 * l'appelant) ; l'envoi effectif est réalisé de façon asynchrone par
 * {@link NotificationOutboxProcessor}.
 */
@Component
public class NotificationServiceAdapter implements NotificationService {

    private final NotificationOutboxRepository repository;
    private final ObjectMapper objectMapper;

    public NotificationServiceAdapter(NotificationOutboxRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void envoyer(Notification notification) {
        String variablesJson = objectMapper.writeValueAsString(notification.variables());
        repository.save(NotificationOutbox.creer(
                notification.canal(), notification.destinataire(), notification.gabarit(), variablesJson));
    }
}
