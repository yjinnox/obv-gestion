package com.obvgestion.application.utilisateur;

import com.obvgestion.application.notification.Notification;
import com.obvgestion.application.notification.NotificationService;
import com.obvgestion.domain.commun.JetonOpaque;
import com.obvgestion.domain.notification.CanalNotification;
import com.obvgestion.domain.utilisateur.ActivationInvalideException;
import com.obvgestion.domain.utilisateur.CanalContact;
import com.obvgestion.domain.utilisateur.CodeOtp;
import com.obvgestion.domain.utilisateur.JetonActivation;
import com.obvgestion.domain.utilisateur.MotDePasseClair;
import com.obvgestion.domain.utilisateur.Utilisateur;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

/**
 * §4.2 — invitation, définition du mot de passe et validation de l'OTP.
 * RG-04 : toute défaillance de jeton ou d'OTP renvoie le même message
 * générique ({@link ActivationInvalideException}), sans énumération de
 * comptes.
 */
@Service
public class ActivationService {

    private final UtilisateurRepository utilisateurRepository;
    private final JetonActivationRepository jetonActivationRepository;
    private final GestionnaireOtp gestionnaireOtp;
    private final HacheurMotDePasse hacheurMotDePasse;
    private final NotificationService notificationService;
    private final String urlFrontend;

    public ActivationService(UtilisateurRepository utilisateurRepository,
                              JetonActivationRepository jetonActivationRepository,
                              GestionnaireOtp gestionnaireOtp,
                              HacheurMotDePasse hacheurMotDePasse,
                              NotificationService notificationService,
                              @Value("${app.frontend-url}") String urlFrontend) {
        this.utilisateurRepository = utilisateurRepository;
        this.jetonActivationRepository = jetonActivationRepository;
        this.gestionnaireOtp = gestionnaireOtp;
        this.hacheurMotDePasse = hacheurMotDePasse;
        this.notificationService = notificationService;
        this.urlFrontend = urlFrontend;
    }

    /** Génère un jeton d'invitation (§4.2) et l'envoie sur le canal de contact de l'utilisateur. */
    @Transactional
    public void inviter(Utilisateur utilisateur) {
        JetonOpaque jeton = JetonOpaque.genererAleatoire();
        jetonActivationRepository.enregistrer(
                JetonActivation.creer(utilisateur, jeton.hacher(), Instant.now()));

        String lienActivation = urlFrontend + "/activation?token=" + jeton.valeurClaire();
        notificationService.envoyer(new Notification(
                canalDe(utilisateur), destinataireDe(utilisateur), "invitation-activation",
                Map.of("nom", utilisateur.getNom(), "prenoms", utilisateur.getPrenoms(),
                        "lienActivation", lienActivation)));
    }

    /** §4.2 étape 3 : définit le mot de passe (RG-02), puis déclenche l'envoi de l'OTP. */
    @Transactional
    public void definirMotDePasse(String tokenClair, String motDePasseClair) {
        JetonActivation jeton = jetonValide(tokenClair);
        Utilisateur utilisateur = jeton.getUtilisateur();

        String hash = hacheurMotDePasse.hacher(new MotDePasseClair(motDePasseClair).valeur());
        utilisateur.definirMotDePasse(hash);
        utilisateurRepository.enregistrer(utilisateur);

        envoyerOtp(utilisateur);
    }

    /** RG-03 — renvoi d'un nouvel OTP (3 fois par heure maximum), sur le jeton d'invitation en cours. */
    @Transactional
    public void renvoyerOtp(String tokenClair) {
        JetonActivation jeton = jetonValide(tokenClair);
        Utilisateur utilisateur = jeton.getUtilisateur();
        if (!gestionnaireOtp.peutRenvoyer(utilisateur.getId())) {
            throw new ActivationInvalideException();
        }
        gestionnaireOtp.enregistrerRenvoi(utilisateur.getId());
        envoyerOtp(utilisateur);
    }

    /** §4.2 étape 4 : valide l'OTP saisi et active définitivement le compte. */
    @Transactional
    public void validerOtp(String tokenClair, String code) {
        JetonActivation jeton = jetonValide(tokenClair);
        Utilisateur utilisateur = jeton.getUtilisateur();

        ResultatVerificationOtp resultat = gestionnaireOtp.verifier(utilisateur.getId(), code);
        if (resultat != ResultatVerificationOtp.VALIDE) {
            throw new ActivationInvalideException();
        }

        utilisateur.activer();
        utilisateurRepository.enregistrer(utilisateur);
        jeton.marquerUtilise(Instant.now());
        jetonActivationRepository.enregistrer(jeton);
    }

    private void envoyerOtp(Utilisateur utilisateur) {
        CodeOtp otp = CodeOtp.genererAleatoire();
        gestionnaireOtp.genererEtStocker(utilisateur.getId(), otp);
        notificationService.envoyer(new Notification(
                canalDe(utilisateur), destinataireDe(utilisateur), "otp",
                Map.of("nom", utilisateur.getNom(), "code", otp.valeur())));
    }

    private JetonActivation jetonValide(String tokenClair) {
        String hash = new JetonOpaque(tokenClair).hacher();
        JetonActivation jeton = jetonActivationRepository.parEmpreinte(hash)
                .orElseThrow(ActivationInvalideException::new);
        if (!jeton.estValide(Instant.now())) {
            throw new ActivationInvalideException();
        }
        return jeton;
    }

    private static CanalNotification canalDe(Utilisateur utilisateur) {
        return utilisateur.getCanalContact() == CanalContact.EMAIL ? CanalNotification.EMAIL : CanalNotification.SMS;
    }

    private static String destinataireDe(Utilisateur utilisateur) {
        return utilisateur.getCanalContact() == CanalContact.EMAIL
                ? utilisateur.getEmail() : utilisateur.getTelephone();
    }
}
