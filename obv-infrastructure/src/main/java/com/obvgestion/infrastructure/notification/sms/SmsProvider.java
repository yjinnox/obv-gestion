package com.obvgestion.infrastructure.notification.sms;

/**
 * §11 — abstraction du fournisseur SMS, sélectionné par la propriété
 * {@code sms.provider} ({@code twilio|orange|log}). Usage interne à
 * l'infrastructure.
 */
public interface SmsProvider {

    void envoyer(String destinataire, String message);
}
