package com.obvgestion.infrastructure.notification.sms;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

/** Fournisseur SMS Twilio (API Messages, https://www.twilio.com/docs/sms/api). */
@Component
@ConditionalOnProperty(prefix = "sms", name = "provider", havingValue = "twilio")
class TwilioSmsProvider implements SmsProvider {

    private final RestClient restClient;
    private final String accountSid;
    private final String numeroExpediteur;

    TwilioSmsProvider(@Value("${sms.twilio.account-sid}") String accountSid,
                       @Value("${sms.twilio.auth-token}") String authToken,
                       @Value("${sms.twilio.from}") String numeroExpediteur) {
        this.accountSid = accountSid;
        this.numeroExpediteur = numeroExpediteur;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.twilio.com/2010-04-01")
                .requestInterceptor(new BasicAuthenticationInterceptor(accountSid, authToken))
                .build();
    }

    @Override
    public void envoyer(String destinataire, String message) {
        MultiValueMap<String, String> corps = new LinkedMultiValueMap<>();
        corps.add("To", destinataire);
        corps.add("From", numeroExpediteur);
        corps.add("Body", message);

        restClient.post()
                .uri("/Accounts/{sid}/Messages.json", accountSid)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(corps)
                .retrieve()
                .toBodilessEntity();
    }
}
