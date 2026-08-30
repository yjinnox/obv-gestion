package com.obvgestion.domain.bar;

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
 * Ligne d'un ticket serveur (§10) : produit, quantité en bouteilles entières
 * (RG-12, pas de demi-unité au bar), prix de vente de la bouteille figé au
 * moment de la saisie (RG-09 par analogie).
 */
@Entity
@Table(name = "ligne_ticket_serveur")
@Audited
public class LigneTicketServeur extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_serveur_id", nullable = false)
    private TicketServeur ticket;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produit_id", nullable = false)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Produit produit;

    @Column(name = "quantite_bouteilles", nullable = false)
    private long quantiteBouteilles;

    @Column(name = "prix_vente_bouteille_xof", nullable = false)
    private long prixVenteBouteilleXof;

    protected LigneTicketServeur() {
    }

    LigneTicketServeur(TicketServeur ticket, Produit produit, long quantiteBouteilles, Montant prixVenteBouteille) {
        if (quantiteBouteilles <= 0) {
            throw new TicketServeurInvalideException("La quantité doit être strictement positive.");
        }
        this.ticket = ticket;
        this.produit = produit;
        this.quantiteBouteilles = quantiteBouteilles;
        this.prixVenteBouteilleXof = prixVenteBouteille.valeurXof();
    }

    public Montant montantLigne() {
        return new Montant(prixVenteBouteilleXof * quantiteBouteilles);
    }

    /** RG-29 (par analogie) — correction des quantités vendues, réservée au SUPER_ADMINISTRATEUR pendant EN_MODIFICATION. */
    void modifierQuantite(long quantiteBouteilles) {
        if (quantiteBouteilles <= 0) {
            throw new TicketServeurInvalideException("La quantité doit être strictement positive.");
        }
        this.quantiteBouteilles = quantiteBouteilles;
    }

    public Long getId() {
        return id;
    }

    public TicketServeur getTicket() {
        return ticket;
    }

    public Produit getProduit() {
        return produit;
    }

    public long getQuantiteBouteilles() {
        return quantiteBouteilles;
    }

    public Montant getPrixVenteBouteille() {
        return new Montant(prixVenteBouteilleXof);
    }
}
