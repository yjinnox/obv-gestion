package com.obvgestion.application.notification;

/**
 * Port de mise en file d'une notification (§11, pattern transactional
 * outbox) : l'implémentation persiste la notification dans la même
 * transaction que l'appelant. L'envoi effectif est asynchrone ; un échec
 * d'envoi ne fait jamais échouer la transaction métier d'origine.
 */
public interface NotificationService {

    void envoyer(Notification notification);
}
