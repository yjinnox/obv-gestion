package com.obvgestion.domain.reception;

import com.obvgestion.domain.audit.Auditable;
import com.obvgestion.domain.utilisateur.Utilisateur;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Duration;
import java.time.Instant;

/**
 * Jeton de demande de validation d'une réception (§7.2 étape 4, RG-35/36).
 * RG-35 : ne porte aucun droit, sert uniquement à faire le lien vers le
 * document dans la notification envoyée au destinataire sélectionné —
 * l'autorisation réelle reste gouvernée par {@code RECEPTION_VALIDER}. Seule
 * l'empreinte du jeton est persistée.
 */
@Entity
@Table(name = "jeton_validation_reception")
public class JetonValidationReception extends Auditable {

    /** RG-36 — TTL du jeton de validation. */
    public static final Duration DUREE_VALIDITE = Duration.ofHours(72);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reception_id", nullable = false)
    private Reception reception;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destinataire_id", nullable = false)
    private Utilisateur destinataire;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "date_expiration", nullable = false)
    private Instant dateExpiration;

    @Column(name = "date_utilisation")
    private Instant dateUtilisation;

    protected JetonValidationReception() {
    }

    private JetonValidationReception(Reception reception, Utilisateur destinataire, String tokenHash,
                                      Instant maintenant) {
        this.reception = reception;
        this.destinataire = destinataire;
        this.tokenHash = tokenHash;
        this.dateExpiration = maintenant.plus(DUREE_VALIDITE);
    }

    public static JetonValidationReception creer(Reception reception, Utilisateur destinataire, String tokenHash,
                                                  Instant maintenant) {
        return new JetonValidationReception(reception, destinataire, tokenHash, maintenant);
    }

    public boolean estValide(Instant maintenant) {
        return dateUtilisation == null && maintenant.isBefore(dateExpiration);
    }

    public Long getId() {
        return id;
    }

    public Reception getReception() {
        return reception;
    }

    public Utilisateur getDestinataire() {
        return destinataire;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public Instant getDateExpiration() {
        return dateExpiration;
    }

    public Instant getDateUtilisation() {
        return dateUtilisation;
    }
}
