package com.obvgestion.domain.transfert;

import com.obvgestion.domain.audit.Auditable;
import com.obvgestion.domain.commun.Montant;
import com.obvgestion.domain.commun.SeparationDesTachesException;
import com.obvgestion.domain.referentiel.Conditionnement;
import com.obvgestion.domain.referentiel.PointDeVente;
import com.obvgestion.domain.referentiel.Produit;
import com.obvgestion.domain.referentiel.TypePointDeVente;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Bon de transfert de marchandise dépôt → bar (§9). RG-32 : workflow
 * identique à la réception ; le stock est mouvementé (RG-31) dès la
 * clôture, avant même la validation du SUPER_ADMINISTRATEUR.
 */
@Entity
@Table(name = "bon_transfert")
@Audited
public class BonTransfert extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "point_de_vente_source_id", nullable = false)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private PointDeVente pointDeVenteSource;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "point_de_vente_destination_id", nullable = false)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private PointDeVente pointDeVenteDestination;

    @Column(name = "date_heure", nullable = false)
    private Instant dateHeure;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private StatutTransfert statut = StatutTransfert.BROUILLON;

    @OneToMany(mappedBy = "transfert", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneTransfert> lignes = new ArrayList<>();

    @Column(name = "motif_annulation")
    private String motifAnnulation;

    @Column(name = "cloturee_par")
    private String clotureePar;

    protected BonTransfert() {
    }

    public BonTransfert(String numero, PointDeVente pointDeVenteSource, PointDeVente pointDeVenteDestination,
                         Instant dateHeure) {
        if (pointDeVenteSource.getType() != TypePointDeVente.DEPOT) {
            throw new TransfertInvalideException(
                    "Le point de vente source d'un transfert doit être un DEPOT.");
        }
        if (pointDeVenteDestination.getType() != TypePointDeVente.BAR) {
            throw new TransfertInvalideException(
                    "Le point de vente destination d'un transfert doit être un BAR.");
        }
        this.numero = numero;
        this.pointDeVenteSource = pointDeVenteSource;
        this.pointDeVenteDestination = pointDeVenteDestination;
        this.dateHeure = dateHeure;
    }

    public LigneTransfert ajouterLigne(Produit produit, Conditionnement conditionnement, long quantiteDemiCasiers,
                                        Montant prixCessionCasier) {
        exigerBrouillon();
        LigneTransfert ligne = new LigneTransfert(this, produit, conditionnement, quantiteDemiCasiers, prixCessionCasier);
        lignes.add(ligne);
        return ligne;
    }

    /** RG-32/RG-17 (par analogie) — bascule en attente de validation ; les mouvements de stock sont appliqués par l'appelant. */
    public void cloturer(String acteur) {
        exigerBrouillon();
        if (lignes.isEmpty()) {
            throw new TransfertInvalideException("Un transfert sans ligne ne peut pas être clôturé.");
        }
        this.statut = StatutTransfert.EN_ATTENTE_VALIDATION;
        this.clotureePar = acteur;
    }

    /** RG-01 — un utilisateur ne peut jamais valider un document qu'il a lui-même clôturé. */
    public void valider(String validateur) {
        exigerEnAttenteValidation();
        if (validateur.equals(clotureePar)) {
            throw new SeparationDesTachesException(
                    "Vous ne pouvez pas valider un transfert que vous avez vous-même clôturé.");
        }
        this.statut = StatutTransfert.VALIDEE;
    }

    /** RG-18 (par analogie) — annulation logique : contre-passation appliquée par l'appelant, motif obligatoire. */
    public void annuler(String motif) {
        exigerEnAttenteValidation();
        if (motif == null || motif.isBlank()) {
            throw new TransfertInvalideException("Le motif d'annulation est obligatoire.");
        }
        this.statut = StatutTransfert.ANNULEE;
        this.motifAnnulation = motif;
    }

    private void exigerBrouillon() {
        if (statut != StatutTransfert.BROUILLON) {
            throw new TransfertInvalideException("Ce transfert ne peut plus être modifié (statut " + statut + ").");
        }
    }

    private void exigerEnAttenteValidation() {
        if (statut != StatutTransfert.EN_ATTENTE_VALIDATION) {
            throw new TransfertInvalideException(
                    "Cette action nécessite un transfert en attente de validation (statut actuel : " + statut + ").");
        }
    }

    public Long getId() {
        return id;
    }

    public String getNumero() {
        return numero;
    }

    public PointDeVente getPointDeVenteSource() {
        return pointDeVenteSource;
    }

    public PointDeVente getPointDeVenteDestination() {
        return pointDeVenteDestination;
    }

    public Instant getDateHeure() {
        return dateHeure;
    }

    public StatutTransfert getStatut() {
        return statut;
    }

    public List<LigneTransfert> getLignes() {
        return List.copyOf(lignes);
    }

    public String getMotifAnnulation() {
        return motifAnnulation;
    }

    public String getClotureePar() {
        return clotureePar;
    }

    public Montant montantTotal() {
        return lignes.stream().map(LigneTransfert::montantLigne).reduce(Montant.zero(), Montant::plus);
    }
}
