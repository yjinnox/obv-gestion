package com.obvgestion.domain.utilisateur;

import com.obvgestion.domain.audit.Auditable;
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
 * Jeton d'invitation à l'activation d'un compte (§4.2). Seule son empreinte
 * ({@link com.obvgestion.domain.commun.JetonOpaque#hacher()}) est
 * persistée.
 */
@Entity
@Table(name = "jeton_activation")
public class JetonActivation extends Auditable {

    /** TTL du jeton d'invitation (§4.2). */
    public static final Duration DUREE_VALIDITE = Duration.ofHours(72);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "date_expiration", nullable = false)
    private Instant dateExpiration;

    @Column(name = "date_utilisation")
    private Instant dateUtilisation;

    protected JetonActivation() {
    }

    private JetonActivation(Utilisateur utilisateur, String tokenHash, Instant maintenant) {
        this.utilisateur = utilisateur;
        this.tokenHash = tokenHash;
        this.dateExpiration = maintenant.plus(DUREE_VALIDITE);
    }

    public static JetonActivation creer(Utilisateur utilisateur, String tokenHash, Instant maintenant) {
        return new JetonActivation(utilisateur, tokenHash, maintenant);
    }

    /**
     * RG-04 — un jeton consommé ou expiré est traité de façon indifférenciée
     * par l'appelant (message générique), mais la validité elle-même se
     * vérifie ici : usage unique et non expiré.
     */
    public boolean estValide(Instant maintenant) {
        return dateUtilisation == null && maintenant.isBefore(dateExpiration);
    }

    public void marquerUtilise(Instant maintenant) {
        if (!estValide(maintenant)) {
            throw new EtatUtilisateurInvalideException("Ce jeton d'activation n'est plus valide.");
        }
        this.dateUtilisation = maintenant;
    }

    public Long getId() {
        return id;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
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
