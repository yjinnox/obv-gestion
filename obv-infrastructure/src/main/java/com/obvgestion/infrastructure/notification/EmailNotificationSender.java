package com.obvgestion.infrastructure.notification;

import com.obvgestion.application.notification.Notification;
import com.obvgestion.domain.notification.CanalNotification;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Locale;
import java.util.Map;

/** §11 — envoi par email (SMTP), gabarits Thymeleaf HTML. */
@Component
@ConditionalOnProperty(prefix = "notification", name = "mode", havingValue = "real", matchIfMissing = true)
class EmailNotificationSender implements NotificationSender {

    private static final Map<String, String> SUJETS = Map.of(
            "invitation-activation", "Activation de votre compte OBV Gestion",
            "otp", "Votre code de vérification OBV Gestion");

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final String expediteur;

    EmailNotificationSender(JavaMailSender mailSender,
                             @Qualifier("templateEngine") TemplateEngine templateEngine,
                             @Value("${notification.email.expediteur}") String expediteur) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.expediteur = expediteur;
    }

    @Override
    public boolean supporte(CanalNotification canal) {
        return canal == CanalNotification.EMAIL;
    }

    @Override
    public void envoyer(Notification notification) {
        Context context = new Context(Locale.FRENCH);
        context.setVariables(notification.variables());
        String corpsHtml = templateEngine.process("email/" + notification.gabarit(), context);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setTo(notification.destinataire());
            helper.setFrom(expediteur);
            helper.setSubject(SUJETS.getOrDefault(notification.gabarit(), "OBV Gestion"));
            helper.setText(corpsHtml, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new IllegalStateException("Échec de construction du message email.", e);
        }
    }
}
