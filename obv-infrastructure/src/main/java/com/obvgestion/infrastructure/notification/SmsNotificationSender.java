package com.obvgestion.infrastructure.notification;

import com.obvgestion.application.notification.Notification;
import com.obvgestion.domain.notification.CanalNotification;
import com.obvgestion.infrastructure.notification.sms.SmsProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Locale;

/** §11 — envoi par SMS, gabarits Thymeleaf en mode texte. */
@Component
@ConditionalOnProperty(prefix = "notification", name = "mode", havingValue = "real", matchIfMissing = true)
class SmsNotificationSender implements NotificationSender {

    private final SmsProvider smsProvider;
    private final TemplateEngine smsTemplateEngine;

    SmsNotificationSender(SmsProvider smsProvider, @Qualifier("smsTemplateEngine") TemplateEngine smsTemplateEngine) {
        this.smsProvider = smsProvider;
        this.smsTemplateEngine = smsTemplateEngine;
    }

    @Override
    public boolean supporte(CanalNotification canal) {
        return canal == CanalNotification.SMS;
    }

    @Override
    public void envoyer(Notification notification) {
        Context context = new Context(Locale.FRENCH);
        context.setVariables(notification.variables());
        String texte = smsTemplateEngine.process(notification.gabarit(), context);
        smsProvider.envoyer(notification.destinataire(), texte);
    }
}
