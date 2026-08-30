package com.obvgestion.infrastructure.notification.sms;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Fournisseur SMS Orange (API SMS, https://developer.orange.com/apis/sms).
 * Le jeton OAuth2 client_credentials est redemandé à chaque envoi : une
 * mise en cache serait souhaitable en volumétrie plus élevée, mais n'a pas
 * été ajoutée ici faute de pouvoir la valider contre l'API réelle.
 */
@Component
@ConditionalOnProperty(prefix = "sms", name = "provider", havingValue = "orange")
class OrangeSmsProvider implements SmsProvider {

    private final RestClient restClient;
    private final String clientId;
    private final String clientSecret;
    private final String adresseExpediteur;

    OrangeSmsProvider(@Value("${sms.orange.client-id}") String clientId,
                       @Value("${sms.orange.client-secret}") String clientSecret,
                       @Value("${sms.orange.sender-address}") String adresseExpediteur) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.adresseExpediteur = adresseExpediteur;
        this.restClient = RestClient.builder().baseUrl("https://api.orange.com").build();
    }

    @Override
    public void envoyer(String destinataire, String message) {
        String jeton = obtenirJetonAcces();

        Map<String, Object> corps = Map.of(
                "outboundSMSMessageRequest", Map.of(
                        "address", "tel:" + destinataire,
                        "senderAddress", "tel:" + adresseExpediteur,
                        "outboundSMSTextMessage", Map.of("message", message)));

        restClient.post()
                .uri("/smsmessaging/v1/outbound/{senderAddress}/requests", "tel:" + adresseExpediteur)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jeton)
                .body(corps)
                .retrieve()
                .toBodilessEntity();
    }

    @SuppressWarnings("unchecked")
    private String obtenirJetonAcces() {
        RestClient client = RestClient.builder()
                .baseUrl("https://api.orange.com")
                .requestInterceptor(new BasicAuthenticationInterceptor(clientId, clientSecret))
                .build();

        MultiValueMap<String, String> corps = new LinkedMultiValueMap<>();
        corps.add("grant_type", "client_credentials");

        Map<String, Object> reponse = client.post()
                .uri("/oauth/v3/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(corps)
                .retrieve()
                .body(Map.class);

        if (reponse == null || reponse.get("access_token") == null) {
            throw new IllegalStateException("Impossible d'obtenir un jeton d'accès Orange SMS API.");
        }
        return (String) reponse.get("access_token");
    }
}
