package com.obvgestion.domain.referentiel;

import com.obvgestion.domain.audit.Auditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Fournisseur du dépôt (§5.1) — absent de la spec initiale, nécessaire pour tracer les réceptions. */
@Entity
@Table(name = "fournisseur")
public class Fournisseur extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "raison_sociale", nullable = false)
    private String raisonSociale;

    private String telephone;

    private String email;

    private String adresse;

    @Column(nullable = false)
    private boolean actif = true;

    protected Fournisseur() {
    }

    public Fournisseur(String raisonSociale, String telephone, String email, String adresse) {
        this.raisonSociale = raisonSociale;
        this.telephone = telephone;
        this.email = email;
        this.adresse = adresse;
    }

    public Long getId() {
        return id;
    }

    public String getRaisonSociale() {
        return raisonSociale;
    }

    public void setRaisonSociale(String raisonSociale) {
        this.raisonSociale = raisonSociale;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }
}
