package com.obvgestion.domain.audit;

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
 * Journal append-only des actions sensibles (§12) : validation, annulation,
 * modification post-clôture, changement de droits, activation/désactivation
 * de compte. Jamais modifié après création.
 */
@Entity
@Table(name = "journal_action")
public class JournalAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String acteur;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TypeActionJournal action;

    @Column(name = "cible_type", nullable = false)
    private String cibleType;

    @Column(name = "cible_id", nullable = false)
    private String cibleId;

    @Column(name = "valeurs_avant", columnDefinition = "text")
    private String valeursAvant;

    @Column(name = "valeurs_apres", columnDefinition = "text")
    private String valeursApres;

    @Column(name = "adresse_ip")
    private String adresseIp;

    @Column(nullable = false)
    private Instant horodatage;

    protected JournalAction() {
    }

    public JournalAction(String acteur, TypeActionJournal action, String cibleType, String cibleId,
                          String valeursAvant, String valeursApres, String adresseIp, Instant horodatage) {
        this.acteur = acteur;
        this.action = action;
        this.cibleType = cibleType;
        this.cibleId = cibleId;
        this.valeursAvant = valeursAvant;
        this.valeursApres = valeursApres;
        this.adresseIp = adresseIp;
        this.horodatage = horodatage;
    }

    public Long getId() {
        return id;
    }

    public String getActeur() {
        return acteur;
    }

    public TypeActionJournal getAction() {
        return action;
    }

    public String getCibleType() {
        return cibleType;
    }

    public String getCibleId() {
        return cibleId;
    }

    public String getValeursAvant() {
        return valeursAvant;
    }

    public String getValeursApres() {
        return valeursApres;
    }

    public String getAdresseIp() {
        return adresseIp;
    }

    public Instant getHorodatage() {
        return horodatage;
    }
}
