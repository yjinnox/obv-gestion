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
import jakarta.persistence.UniqueConstraint;

/** Couple Marque + Volume (§1, §5.1), ex. « Flag 33 cl » : unité de référence du catalogue. */
@Entity
@Table(name = "produit", uniqueConstraints = @UniqueConstraint(columnNames = {"marque_id", "volume_id"}))
public class Produit extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "marque_id", nullable = false)
    private Marque marque;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "volume_id", nullable = false)
    private Volume volume;

    @Column(nullable = false)
    private boolean actif = true;

    protected Produit() {
    }

    public Produit(Marque marque, Volume volume) {
        this.marque = marque;
        this.volume = volume;
    }

    public Long getId() {
        return id;
    }

    public Marque getMarque() {
        return marque;
    }

    public Volume getVolume() {
        return volume;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }
}
