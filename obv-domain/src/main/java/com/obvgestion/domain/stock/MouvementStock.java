package com.obvgestion.domain.stock;

import com.obvgestion.domain.referentiel.PointDeVente;
import com.obvgestion.domain.referentiel.Produit;
import com.obvgestion.domain.utilisateur.Utilisateur;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Journal append-only des mouvements de stock (§6.2), jamais modifié après
 * création. RG-14 : le solde de {@link Stock} doit toujours être égal à la
 * somme des mouvements.
 */
@Entity
@Table(name = "mouvement_stock")
public class MouvementStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "point_de_vente_id", nullable = false)
    private PointDeVente pointDeVente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeMouvementStock type;

    @Column(name = "quantite_signee", nullable = false)
    private long quantiteSignee;

    @Column(name = "stock_avant", nullable = false)
    private long stockAvant;

    @Column(name = "stock_apres", nullable = false)
    private long stockApres;

    @Column(name = "document_type", nullable = false)
    private String documentType;

    @Column(name = "document_id", nullable = false)
    private Long documentId;

    @Column(name = "date_heure", nullable = false)
    private Instant dateHeure;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    protected MouvementStock() {
    }

    public MouvementStock(PointDeVente pointDeVente, Produit produit, TypeMouvementStock type, long quantiteSignee,
                           long stockAvant, long stockApres, String documentType, Long documentId,
                           Instant dateHeure, Utilisateur utilisateur) {
        this.pointDeVente = pointDeVente;
        this.produit = produit;
        this.type = type;
        this.quantiteSignee = quantiteSignee;
        this.stockAvant = stockAvant;
        this.stockApres = stockApres;
        this.documentType = documentType;
        this.documentId = documentId;
        this.dateHeure = dateHeure;
        this.utilisateur = utilisateur;
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

    public TypeMouvementStock getType() {
        return type;
    }

    public long getQuantiteSignee() {
        return quantiteSignee;
    }

    public long getStockAvant() {
        return stockAvant;
    }

    public long getStockApres() {
        return stockApres;
    }

    public String getDocumentType() {
        return documentType;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public Instant getDateHeure() {
        return dateHeure;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }
}
