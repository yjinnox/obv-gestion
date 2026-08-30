package com.obvgestion.domain.referentiel;

import com.obvgestion.domain.audit.Auditable;
import com.obvgestion.domain.commun.Montant;
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
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import java.time.LocalDate;

/**
 * Tarif daté (§5.2) : le prix n'est jamais un simple attribut du produit ou
 * du casier — une modification de prix ne doit jamais altérer les
 * documents passés (RG-09, figés au moment de leur création).
 */
@Entity
@Table(name = "tarif")
@Audited
public class Tarif extends Auditable {

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

    @Enumerated(EnumType.STRING)
    @Column(name = "unite_vente", nullable = false, length = 20)
    private UniteVente uniteVente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private NatureTarif nature;

    @Column(name = "montant_xof", nullable = false)
    private long montantXof;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    protected Tarif() {
    }

    private Tarif(PointDeVente pointDeVente, Produit produit, UniteVente uniteVente, NatureTarif nature,
                   Montant montant, LocalDate dateDebut) {
        this.pointDeVente = pointDeVente;
        this.produit = produit;
        this.uniteVente = uniteVente;
        this.nature = nature;
        this.montantXof = montant.valeurXof();
        this.dateDebut = dateDebut;
    }

    /**
     * RG-10 — un point de vente de type DEPOT n'utilise que
     * {@code uniteVente = CASIER}, un BAR que {@code BOUTEILLE}.
     */
    public static Tarif creer(PointDeVente pointDeVente, Produit produit, UniteVente uniteVente, NatureTarif nature,
                               Montant montant, LocalDate dateDebut) {
        UniteVente attendue = pointDeVente.getType() == TypePointDeVente.DEPOT
                ? UniteVente.CASIER : UniteVente.BOUTEILLE;
        if (uniteVente != attendue) {
            throw new TarifInvalideException(
                    "Un point de vente de type " + pointDeVente.getType()
                            + " n'utilise que l'unité de vente " + attendue + ".");
        }
        return new Tarif(pointDeVente, produit, uniteVente, nature, montant, dateDebut);
    }

    /** RG-08 — clôt le tarif courant : appelé avant la création du tarif qui lui succède. */
    public void cloturer(LocalDate dateFin) {
        if (this.dateFin != null) {
            throw new TarifInvalideException("Ce tarif est déjà clos.");
        }
        if (dateFin.isBefore(dateDebut)) {
            throw new TarifInvalideException("La date de clôture ne peut pas précéder la date de début.");
        }
        this.dateFin = dateFin;
    }

    /** Borne de fin exclusive : un tarif clos le {@code dateFin} n'est plus actif à cette date. */
    public boolean estActif(LocalDate date) {
        return !date.isBefore(dateDebut) && (dateFin == null || date.isBefore(dateFin));
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

    public UniteVente getUniteVente() {
        return uniteVente;
    }

    public NatureTarif getNature() {
        return nature;
    }

    public Montant getMontant() {
        return new Montant(montantXof);
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }
}
