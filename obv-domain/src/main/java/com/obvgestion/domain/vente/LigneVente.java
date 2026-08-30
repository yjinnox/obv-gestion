package com.obvgestion.domain.vente;

import com.obvgestion.domain.audit.Auditable;
import com.obvgestion.domain.commun.Montant;
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
 * Ligne d'une vente au dépôt (§8.2) : produit, quantité, prix de vente et
 * consigne figés au moment de la commande (RG-09 par analogie). RG-11 : la
 * quantité est stockée en demi-casiers entiers (unité canonique du stock
 * dépôt), jamais en casiers décimaux — {@code 1} = demi-casier, {@code 2} =
 * un casier ; le demi-casier n'est autorisé que si RG-13 l'permet, ce que
 * vérifie la couche applicative (accès au référentiel requis).
 */
@Entity
@Table(name = "ligne_vente")
@Audited
public class LigneVente extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vente_id", nullable = false)
    private Vente vente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produit_id", nullable = false)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Produit produit;

    @Column(name = "quantite_demi_casiers", nullable = false)
    private long quantiteDemiCasiers;

    @Column(name = "prix_vente_casier_xof", nullable = false)
    private long prixVenteCasierXof;

    @Column(name = "montant_consigne_casier_xof", nullable = false)
    private long montantConsigneCasierXof;

    protected LigneVente() {
    }

    LigneVente(Vente vente, Produit produit, long quantiteDemiCasiers, Montant prixVenteCasier,
               Montant montantConsigneCasier) {
        if (quantiteDemiCasiers <= 0) {
            throw new VenteInvalideException("La quantité doit être strictement positive.");
        }
        this.vente = vente;
        this.produit = produit;
        this.quantiteDemiCasiers = quantiteDemiCasiers;
        this.prixVenteCasierXof = prixVenteCasier.valeurXof();
        this.montantConsigneCasierXof = montantConsigneCasier.valeurXof();
    }

    public long quantiteDemiCasiers() {
        return quantiteDemiCasiers;
    }

    /** Prix au prorata du demi-casier (division entière : H1, jamais de décimale en XOF). */
    public Montant montantLigne() {
        return new Montant(prixVenteCasierXof * quantiteDemiCasiers / 2);
    }

    public Montant montantConsigneLigne() {
        return new Montant(montantConsigneCasierXof * quantiteDemiCasiers / 2);
    }

    /** RG-29 — correction des quantités vendues, réservée au SUPER_ADMINISTRATEUR pendant EN_MODIFICATION. */
    void modifierQuantite(long quantiteDemiCasiers) {
        if (quantiteDemiCasiers <= 0) {
            throw new VenteInvalideException("La quantité doit être strictement positive.");
        }
        this.quantiteDemiCasiers = quantiteDemiCasiers;
    }

    public Long getId() {
        return id;
    }

    public Vente getVente() {
        return vente;
    }

    public Produit getProduit() {
        return produit;
    }

    public Montant getPrixVenteCasier() {
        return new Montant(prixVenteCasierXof);
    }

    public Montant getMontantConsigneCasier() {
        return new Montant(montantConsigneCasierXof);
    }
}
