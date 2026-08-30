package com.obvgestion.domain.referentiel;

import com.obvgestion.domain.audit.Auditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Volume d'un produit (§5.1), ex. « 33 cl » = 330 ml. */
@Entity
@Table(name = "volume")
public class Volume extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String libelle;

    @Column(name = "contenance_ml", nullable = false)
    private int contenanceMl;

    @Column(nullable = false)
    private boolean actif = true;

    protected Volume() {
    }

    public Volume(String libelle, int contenanceMl) {
        if (contenanceMl <= 0) {
            throw new IllegalArgumentException("La contenance doit être strictement positive.");
        }
        this.libelle = libelle;
        this.contenanceMl = contenanceMl;
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

    public int getContenanceMl() {
        return contenanceMl;
    }

    public void setContenanceMl(int contenanceMl) {
        this.contenanceMl = contenanceMl;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }
}
