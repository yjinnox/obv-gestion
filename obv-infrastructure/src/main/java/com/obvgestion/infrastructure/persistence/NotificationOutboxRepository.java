package com.obvgestion.infrastructure.persistence;

import com.obvgestion.domain.notification.NotificationOutbox;
import com.obvgestion.domain.notification.StatutNotificationOutbox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, Long> {

    List<NotificationOutbox> findTop50ByStatutOrderByCreatedAtAsc(StatutNotificationOutbox statut);

    List<NotificationOutbox> findByDestinataireAndGabaritOrderByCreatedAtDesc(String destinataire, String gabarit);
}
