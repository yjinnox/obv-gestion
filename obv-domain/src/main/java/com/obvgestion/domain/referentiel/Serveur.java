package com.obvgestion.domain.referentiel;

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

/**
 * Serveur d'un bar/maquis (§1, §5.1) : modélisé comme référentiel et non
 * comme compte applicatif en v1 (§3.1), sélectionné par le gérant pour
 * ouvrir un ticket (§10).
 */
@Entity
@Table(name = "serveur")
public class Serveur extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "point_de_vente_id", nullable = false)
    private PointDeVente pointDeVente;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenoms;

    private String telephone;

    @Column(nullable = false)
    private boolean actif = true;

    protected Serveur() {
    }

    public Serveur(PointDeVente pointDeVente, String nom, String prenoms, String telephone) {
        this.pointDeVente = pointDeVente;
        this.nom = nom;
        this.prenoms = prenoms;
        this.telephone = telephone;
    }

    public Long getId() {
        return id;
    }

    public PointDeVente getPointDeVente() {
        return pointDeVente;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenoms() {
        return prenoms;
    }

    public void setPrenoms(String prenoms) {
        this.prenoms = prenoms;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }
}
