package com.obvgestion.infrastructure.notification;

import com.obvgestion.application.notification.Notification;
import com.obvgestion.domain.notification.NotificationOutbox;
import com.obvgestion.domain.notification.StatutNotificationOutbox;
import com.obvgestion.infrastructure.persistence.NotificationOutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * §11 — job de relance du pattern transactional outbox : traite les
 * notifications en attente par lots de 50, jusqu'à {@value
 * com.obvgestion.domain.notification.NotificationOutbox#TENTATIVES_MAX}
 * tentatives avec recul (l'intervalle fixe entre exécutions fait office de
 * backoff). Un échec d'envoi individuel n'interrompt jamais le traitement
 * du lot ni la transaction métier d'origine (déjà validée à l'écriture de
 * l'entrée).
 */
@Component
class NotificationOutboxProcessor {

    private static final Logger log = LoggerFactory.getLogger(NotificationOutboxProcessor.class);

    private final NotificationOutboxRepository repository;
    private final List<NotificationSender> senders;
    private final ObjectMapper objectMapper;

    NotificationOutboxProcessor(NotificationOutboxRepository repository, List<NotificationSender> senders,
                                 ObjectMapper objectMapper) {
        this.repository = repository;
        this.senders = senders;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelayString = "${notification.outbox.intervalle-ms:15000}")
    @Transactional
    void traiterLotEnAttente() {
        List<NotificationOutbox> lot =
                repository.findTop50ByStatutOrderByCreatedAtAsc(StatutNotificationOutbox.EN_ATTENTE);
        for (NotificationOutbox entree : lot) {
            traiterUneEntree(entree);
        }
    }

    @SuppressWarnings("unchecked")
    private void traiterUneEntree(NotificationOutbox entree) {
        try {
            NotificationSender sender = senders.stream()
                    .filter(s -> s.supporte(entree.getCanal()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "Aucun expéditeur disponible pour le canal " + entree.getCanal()));

            Map<String, Object> variables = objectMapper.readValue(entree.getVariablesJson(), Map.class);
            Notification notification =
                    new Notification(entree.getCanal(), entree.getDestinataire(), entree.getGabarit(), variables);

            sender.envoyer(notification);
            entree.marquerEnvoye(Instant.now());
        } catch (Exception e) {
            log.warn("Échec d'envoi de la notification {} (tentative {}/{}) : {}",
                    entree.getId(), entree.getTentatives() + 1, NotificationOutbox.TENTATIVES_MAX, e.getMessage());
            entree.enregistrerEchec(e.getMessage());
        }
        repository.save(entree);
    }
}
