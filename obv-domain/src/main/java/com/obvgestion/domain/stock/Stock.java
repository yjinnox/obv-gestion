package com.obvgestion.domain.stock;

import com.obvgestion.domain.audit.Auditable;
import com.obvgestion.domain.referentiel.PointDeVente;
import com.obvgestion.domain.referentiel.Produit;
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
import jakarta.persistence.Version;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

/**
 * Solde de stock d'un produit sur un point de vente (§6.2) : demi-casiers
 * entiers au dépôt (RG-11), bouteilles entières au bar (RG-12). Le verrou
 * optimiste ({@link #version}) protège les décrémentations concurrentes
 * (RG-16).
 */
@Entity
@Table(name = "stock", uniqueConstraints = @UniqueConstraint(columnNames = {"point_de_vente_id", "produit_id"}))
@Audited
public class Stock extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "point_de_vente_id", nullable = false)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private PointDeVente pointDeVente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produit_id", nullable = false)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Produit produit;

    @Column(nullable = false)
    private long quantite;

    @Version
    @Column(nullable = false)
    private long version;

    protected Stock() {
    }

    public Stock(PointDeVente pointDeVente, Produit produit, long quantite) {
        this.pointDeVente = pointDeVente;
        this.produit = produit;
        this.quantite = quantite;
    }

    /**
     * RG-14/RG-15 — {@code quantiteSignee} positive pour une entrée, négative
     * pour une sortie ; rejette toute opération qui rendrait le stock négatif.
     */
    public void appliquer(long quantiteSignee) {
        long nouvelleQuantite = this.quantite + quantiteSignee;
        if (nouvelleQuantite < 0) {
            throw new StockInsuffisantException(libelleProduit(), -quantiteSignee, this.quantite);
        }
        this.quantite = nouvelleQuantite;
    }

    private String libelleProduit() {
        return produit.getMarque().getLibelle() + " " + produit.getVolume().getLibelle();
    }

    public Long getId() {
        return id;
    }

    public PointDeVente getPointDeVente() {
        return pointDeVente;
    }

    public Produit getProduit() {
        return produit;
    }

    public long getQuantite() {
        return quantite;
    }

    public long getVersion() {
        return version;
    }
}
