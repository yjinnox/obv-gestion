package com.obvgestion.domain.reception;

import com.obvgestion.domain.audit.Auditable;
import com.obvgestion.domain.commun.Montant;
import com.obvgestion.domain.referentiel.Conditionnement;
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
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

/**
 * Ligne d'une réception (§7.2 étape 2) : produit, conditionnement, nombre de
 * casiers entiers, prix d'achat du casier figé au moment de la saisie
 * (pré-rempli depuis le tarif ACHAT en vigueur, modifiable).
 */
@Entity
@Table(name = "ligne_reception")
@Audited
public class LigneReception extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reception_id", nullable = false)
    private Reception reception;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produit_id", nullable = false)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Produit produit;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conditionnement_id", nullable = false)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Conditionnement conditionnement;

    @Column(name = "nombre_casiers", nullable = false)
    private long nombreCasiers;

    @Column(name = "prix_achat_casier_xof", nullable = false)
    private long prixAchatCasierXof;

    protected LigneReception() {
    }

    LigneReception(Reception reception, Produit produit, Conditionnement conditionnement, long nombreCasiers,
                    Montant prixAchatCasier) {
        if (nombreCasiers <= 0) {
            throw new ReceptionInvalideException("Le nombre de casiers doit être strictement positif.");
        }
        this.reception = reception;
        this.produit = produit;
        this.conditionnement = conditionnement;
        this.nombreCasiers = nombreCasiers;
        this.prixAchatCasierXof = prixAchatCasier.valeurXof();
    }

    void modifier(long nombreCasiers, Montant prixAchatCasier) {
        if (nombreCasiers <= 0) {
            throw new ReceptionInvalideException("Le nombre de casiers doit être strictement positif.");
        }
        this.nombreCasiers = nombreCasiers;
        this.prixAchatCasierXof = prixAchatCasier.valeurXof();
    }

    /** RG-11 — la conversion en demi-casiers pour le mouvement de stock se fait dans la couche applicative. */
    public long quantiteDemiCasiers() {
        return nombreCasiers * 2;
    }

    public Montant montantLigne() {
        return new Montant(prixAchatCasierXof).multiplie(nombreCasiers);
    }

    public Long getId() {
        return id;
    }

    public Reception getReception() {
        return reception;
    }

    public Produit getProduit() {
        return produit;
    }

    public Conditionnement getConditionnement() {
        return conditionnement;
    }

    public long getNombreCasiers() {
        return nombreCasiers;
    }

    public Montant getPrixAchatCasier() {
        return new Montant(prixAchatCasierXof);
    }
}
