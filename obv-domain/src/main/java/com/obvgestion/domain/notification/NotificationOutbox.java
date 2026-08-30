package com.obvgestion.domain.notification;

import com.obvgestion.domain.audit.Auditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * File d'attente transactionnelle des notifications (§11, pattern
 * transactional outbox) : écrite dans la même transaction que l'action
 * métier qui la déclenche, traitée ensuite de façon asynchrone par un job
 * de relance (3 tentatives avec backoff). Un échec d'envoi ne fait jamais
 * échouer la transaction métier d'origine.
 */
@Entity
@Table(name = "notification_outbox")
public class NotificationOutbox extends Auditable {

    public static final int TENTATIVES_MAX = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CanalNotification canal;

    @Column(nullable = false)
    private String destinataire;

    @Column(nullable = false)
    private String gabarit;

    @Column(name = "variables_json", nullable = false, columnDefinition = "text")
    private String variablesJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutNotificationOutbox statut;

    @Column(nullable = false)
    private int tentatives = 0;

    @Column(name = "derniere_erreur")
    private String derniereErreur;

    @Column(name = "date_envoi")
    private Instant dateEnvoi;

    protected NotificationOutbox() {
    }

    private NotificationOutbox(CanalNotification canal, String destinataire, String gabarit, String variablesJson) {
        this.canal = canal;
        this.destinataire = destinataire;
        this.gabarit = gabarit;
        this.variablesJson = variablesJson;
        this.statut = StatutNotificationOutbox.EN_ATTENTE;
    }

    public static NotificationOutbox creer(CanalNotification canal, String destinataire, String gabarit,
                                            String variablesJson) {
        return new NotificationOutbox(canal, destinataire, gabarit, variablesJson);
    }

    public void marquerEnvoye(Instant maintenant) {
        this.statut = StatutNotificationOutbox.ENVOYE;
        this.dateEnvoi = maintenant;
    }

    /** Enregistre un échec ; bascule en échec définitif après {@value #TENTATIVES_MAX} tentatives. */
    public void enregistrerEchec(String erreur) {
        this.tentatives++;
        this.derniereErreur = erreur;
        if (this.tentatives >= TENTATIVES_MAX) {
            this.statut = StatutNotificationOutbox.ECHEC_DEFINITIF;
        }
    }

    public boolean estEnAttente() {
        return statut == StatutNotificationOutbox.EN_ATTENTE;
    }

    public Long getId() {
        return id;
    }

    public CanalNotification getCanal() {
        return canal;
    }

    public String getDestinataire() {
        return destinataire;
    }

    public String getGabarit() {
        return gabarit;
    }

    public String getVariablesJson() {
        return variablesJson;
    }

    public StatutNotificationOutbox getStatut() {
        return statut;
    }

    public int getTentatives() {
        return tentatives;
    }

    public String getDerniereErreur() {
        return derniereErreur;
    }

    public Instant getDateEnvoi() {
        return dateEnvoi;
    }
}
