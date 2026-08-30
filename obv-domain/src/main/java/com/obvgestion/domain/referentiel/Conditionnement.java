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

/** Casier d'un produit donné (§1, §5.1), caractérisé par sa capacité en bouteilles (12, 16, 24…). */
@Entity
@Table(name = "conditionnement")
public class Conditionnement extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @Column(name = "capacite_bouteilles", nullable = false)
    private int capaciteBouteilles;

    @Column(nullable = false)
    private boolean actif = true;

    protected Conditionnement() {
    }

    public Conditionnement(Produit produit, int capaciteBouteilles) {
        if (capaciteBouteilles <= 0) {
            throw new IllegalArgumentException("La capacité doit être strictement positive.");
        }
        this.produit = produit;
        this.capaciteBouteilles = capaciteBouteilles;
    }

    public Long getId() {
        return id;
    }

    public Produit getProduit() {
        return produit;
    }

    public int getCapaciteBouteilles() {
        return capaciteBouteilles;
    }

    /** §5.1 — calculé, jamais saisi : le demi-casier n'est autorisé que si la capacité est paire. */
    public boolean isDemiCasierAutorise() {
        return capaciteBouteilles % 2 == 0;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }
}
