package com.obvgestion.domain.transfert;

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
 * Ligne d'un transfert (§9) : produit, conditionnement, quantité sortie du
 * dépôt en demi-casiers entiers (RG-11), quantité entrant au bar en
 * bouteilles entières (RG-12), prix de cession du casier figé au moment de
 * la saisie (RG-09).
 */
@Entity
@Table(name = "ligne_transfert")
@Audited
public class LigneTransfert extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transfert_id", nullable = false)
    private BonTransfert transfert;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produit_id", nullable = false)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Produit produit;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conditionnement_id", nullable = false)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Conditionnement conditionnement;

    @Column(name = "quantite_demi_casiers", nullable = false)
    private long quantiteDemiCasiers;

    @Column(name = "quantite_bouteilles", nullable = false)
    private long quantiteBouteilles;

    @Column(name = "prix_cession_casier_xof", nullable = false)
    private long prixCessionCasierXof;

    protected LigneTransfert() {
    }

    /**
     * RG-30 — {@code quantiteBouteilles = (quantiteDemiCasiers × capaciteBouteilles) / 2},
     * refusé si non entier. Cette même division rejette implicitement toute
     * transformation d'un demi-casier interdit par le conditionnement
     * (RG-13) : si {@code capaciteBouteilles} est impair (demi-casier non
     * autorisé), le produit n'est entier que pour une {@code quantiteDemiCasiers} paire.
     */
    LigneTransfert(BonTransfert transfert, Produit produit, Conditionnement conditionnement, long quantiteDemiCasiers,
                    Montant prixCessionCasier) {
        if (quantiteDemiCasiers <= 0) {
            throw new TransfertInvalideException("La quantité transférée doit être strictement positive.");
        }
        long produitBouteilles = quantiteDemiCasiers * conditionnement.getCapaciteBouteilles();
        if (produitBouteilles % 2 != 0) {
            throw new TransfertInvalideException(
                    "La quantité transférée (" + quantiteDemiCasiers + " demi-casier(s) de "
                            + conditionnement.getCapaciteBouteilles() + " bouteilles) ne correspond pas à un nombre "
                            + "entier de bouteilles.");
        }
        this.transfert = transfert;
        this.produit = produit;
        this.conditionnement = conditionnement;
        this.quantiteDemiCasiers = quantiteDemiCasiers;
        this.quantiteBouteilles = produitBouteilles / 2;
        this.prixCessionCasierXof = prixCessionCasier.valeurXof();
    }

    /** Proratisé comme {@code LigneVente.montantLigne()} : moitié du prix du casier pour un demi-casier. */
    public Montant montantLigne() {
        return new Montant(prixCessionCasierXof * quantiteDemiCasiers / 2);
    }

    public Long getId() {
        return id;
    }

    public BonTransfert getTransfert() {
        return transfert;
    }

    public Produit getProduit() {
        return produit;
    }

    public Conditionnement getConditionnement() {
        return conditionnement;
    }

    public long getQuantiteDemiCasiers() {
        return quantiteDemiCasiers;
    }

    public long getQuantiteBouteilles() {
        return quantiteBouteilles;
    }

    public Montant getPrixCessionCasier() {
        return new Montant(prixCessionCasierXof);
    }
}
