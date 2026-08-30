package com.obvgestion.domain.referentiel;

import com.obvgestion.domain.audit.Auditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Point de vente (§1, §5.1) : un dépôt (vend au casier/demi-casier) ou un
 * bar/maquis (vend à la bouteille). Introduit dès P1 car les comptes
 * utilisateurs y sont rattachés (H10) ; le CRUD complet du référentiel
 * arrive en P2.
 */
@Entity
@Table(name = "point_de_vente")
public class PointDeVente extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String libelle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypePointDeVente type;

    private String adresse;

    @Column(nullable = false)
    private boolean actif = true;

    protected PointDeVente() {
    }

    public PointDeVente(String libelle, TypePointDeVente type, String adresse) {
        this.libelle = libelle;
        this.type = type;
        this.adresse = adresse;
    }

    public Long getId() {
        return id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public TypePointDeVente getType() {
        return type;
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
