package com.obvgestion.domain.reception;

import com.obvgestion.domain.audit.Auditable;
import com.obvgestion.domain.commun.Montant;
import com.obvgestion.domain.commun.SeparationDesTachesException;
import com.obvgestion.domain.referentiel.Conditionnement;
import com.obvgestion.domain.referentiel.Fournisseur;
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
 * Réception de marchandise au dépôt (§7). RG-17 : le stock est incrémenté
 * dès la clôture par le gérant, avant même la validation du
 * SUPER_ADMINISTRATEUR — cette classe ne pilote que le cycle de vie du
 * document ; l'application des mouvements de stock associés est orchestrée
 * par le service applicatif dans la même opération.
 */
@Entity
@Table(name = "reception")
@Audited
public class Reception extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fournisseur_id", nullable = false)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Fournisseur fournisseur;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "point_de_vente_id", nullable = false)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private PointDeVente pointDeVente;

    @Column(name = "date_heure_livraison", nullable = false)
    private Instant dateHeureLivraison;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private StatutReception statut = StatutReception.BROUILLON;

    @OneToMany(mappedBy = "reception", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneReception> lignes = new ArrayList<>();

    @Column(name = "motif_annulation")
    private String motifAnnulation;

    @Column(name = "cloturee_par")
    private String clotureePar;

    protected Reception() {
    }

    public Reception(Fournisseur fournisseur, PointDeVente pointDeVente, Instant dateHeureLivraison) {
        if (pointDeVente.getType() != TypePointDeVente.DEPOT) {
            throw new ReceptionInvalideException(
                    "Une réception ne peut être créée que pour un point de vente de type DEPOT.");
        }
        this.fournisseur = fournisseur;
        this.pointDeVente = pointDeVente;
        this.dateHeureLivraison = dateHeureLivraison;
    }

    public LigneReception ajouterLigne(Produit produit, Conditionnement conditionnement, long nombreCasiers,
                                        Montant prixAchatCasier) {
        exigerModifiable();
        LigneReception ligne = new LigneReception(this, produit, conditionnement, nombreCasiers, prixAchatCasier);
        lignes.add(ligne);
        return ligne;
    }

    public void modifierLigne(Long ligneId, long nombreCasiers, Montant prixAchatCasier) {
        exigerModifiable();
        ligneParId(ligneId).modifier(nombreCasiers, prixAchatCasier);
    }

    /** §13 — suppression réservée au brouillon ; en attente de validation, une ligne se corrige à zéro (RG-20). */
    public void supprimerLigne(Long ligneId) {
        if (statut != StatutReception.BROUILLON) {
            throw new ReceptionInvalideException("Une ligne ne peut être supprimée qu'en brouillon.");
        }
        lignes.remove(ligneParId(ligneId));
    }

    /** RG-17 — bascule en attente de validation ; l'incrément de stock est appliqué par l'appelant. */
    public void cloturer(String acteur) {
        if (statut != StatutReception.BROUILLON) {
            throw new ReceptionInvalideException("Seule une réception en brouillon peut être clôturée.");
        }
        if (lignes.isEmpty()) {
            throw new ReceptionInvalideException("Une réception sans ligne ne peut pas être clôturée.");
        }
        this.statut = StatutReception.EN_ATTENTE_VALIDATION;
        this.clotureePar = acteur;
    }

    /** RG-01 — un utilisateur ne peut jamais valider un document qu'il a lui-même clôturé. */
    public void valider(String validateur) {
        exigerEnAttenteValidation();
        if (validateur.equals(clotureePar)) {
            throw new SeparationDesTachesException(
                    "Vous ne pouvez pas valider une réception que vous avez vous-même clôturée.");
        }
        this.statut = StatutReception.VALIDEE;
    }

    /** RG-18/RG-22 — annulation logique (contre-passation appliquée par l'appelant), motif obligatoire. */
    public void annuler(String motif) {
        exigerEnAttenteValidation();
        if (motif == null || motif.isBlank()) {
            throw new ReceptionInvalideException("Le motif d'annulation est obligatoire.");
        }
        this.statut = StatutReception.ANNULEE;
        this.motifAnnulation = motif;
    }

    private void exigerModifiable() {
        if (statut != StatutReception.BROUILLON && statut != StatutReception.EN_ATTENTE_VALIDATION) {
            throw new ReceptionInvalideException(
                    "Cette réception ne peut plus être modifiée (statut " + statut + ").");
        }
    }

    private void exigerEnAttenteValidation() {
        if (statut != StatutReception.EN_ATTENTE_VALIDATION) {
            throw new ReceptionInvalideException(
                    "Cette action nécessite une réception en attente de validation (statut actuel : " + statut + ").");
        }
    }

    private LigneReception ligneParId(Long ligneId) {
        return lignes.stream()
                .filter(ligne -> ligne.getId() != null && ligne.getId().equals(ligneId))
                .findFirst()
                .orElseThrow(() -> new ReceptionInvalideException("Ligne introuvable : " + ligneId));
    }

    public Long getId() {
        return id;
    }

    public Fournisseur getFournisseur() {
        return fournisseur;
    }

    public PointDeVente getPointDeVente() {
        return pointDeVente;
    }

    public Instant getDateHeureLivraison() {
        return dateHeureLivraison;
    }

    public StatutReception getStatut() {
        return statut;
    }

    public List<LigneReception> getLignes() {
        return List.copyOf(lignes);
    }

    public String getMotifAnnulation() {
        return motifAnnulation;
    }

    public String getClotureePar() {
        return clotureePar;
    }

    public Montant montantTotal() {
        return lignes.stream().map(LigneReception::montantLigne).reduce(Montant.zero(), Montant::plus);
    }
}
