package com.obvgestion.domain.vente;

import com.obvgestion.domain.referentiel.PointDeVente;
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
import jakarta.persistence.UniqueConstraint;

/**
 * RG-26 — compteur de numérotation des documents de vente
 * ({@code BC-{PDV}-{AAAA}-{séquence}} / {@code FA-{PDV}-{AAAA}-{séquence}}),
 * dédié par point de vente et par année. Un compteur en base avec verrou
 * pessimiste (plutôt qu'une séquence PostgreSQL native) garantit qu'un
 * incrément n'est jamais conservé si la transaction qui l'a demandé échoue
 * ensuite : aucun trou toléré dans la numérotation.
 */
@Entity
@Table(name = "compteur_document",
        uniqueConstraints = @UniqueConstraint(columnNames = {"point_de_vente_id", "type", "annee"}))
public class CompteurDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "point_de_vente_id", nullable = false)
    private PointDeVente pointDeVente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeNumeroDocument type;

    @Column(nullable = false)
    private int annee;

    @Column(name = "dernier_numero", nullable = false)
    private long dernierNumero;

    protected CompteurDocument() {
    }

    public CompteurDocument(PointDeVente pointDeVente, TypeNumeroDocument type, int annee) {
        this.pointDeVente = pointDeVente;
        this.type = type;
        this.annee = annee;
        this.dernierNumero = 0;
    }

    /** À appeler sous verrou pessimiste (voir le port de persistance) : incrémente et retourne le nouveau numéro. */
    public long incrementerEtObtenir() {
        return ++dernierNumero;
    }

    public String formaterNumero(Long pointDeVenteId, long numero) {
        return "%s-%d-%d-%06d".formatted(type.prefixe(), pointDeVenteId, annee, numero);
    }

    public Long getId() {
        return id;
    }

    public PointDeVente getPointDeVente() {
        return pointDeVente;
    }

    public TypeNumeroDocument getType() {
        return type;
    }

    public int getAnnee() {
        return annee;
    }

    public long getDernierNumero() {
        return dernierNumero;
    }
}
