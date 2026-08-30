package com.obvgestion.domain.vente;

import com.obvgestion.domain.audit.Auditable;
import com.obvgestion.domain.commun.Montant;
import com.obvgestion.domain.referentiel.Client;
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
import jakarta.persistence.UniqueConstraint;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Vente au dépôt (§8.2, appelée « commande » dans l'IHM/l'API
 * {@code /commandes}) : née de la validation du panier. RG-24 : le stock
 * est décrémenté par l'appelant au moment de la création, jamais à l'ajout
 * au panier. RG-27 : l'idempotence est garantie par un index unique sur
 * {@code (sessionVente, idempotencyKey)}, appliqué en base.
 */
@Entity
@Table(name = "vente", uniqueConstraints = @UniqueConstraint(columnNames = {"session_vente_id", "idempotency_key"}))
@Audited
public class Vente extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_vente_id", nullable = false)
    private SessionVente sessionVente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Client client;

    @Column(name = "numero_bon_commande", nullable = false, unique = true)
    private String numeroBonCommande;

    @Column(name = "numero_facture", nullable = false, unique = true)
    private String numeroFacture;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode_paiement", nullable = false, length = 20)
    private ModePaiement modePaiement;

    @Column(name = "montant_sous_total_xof", nullable = false)
    private long montantSousTotalXof;

    @Column(name = "montant_consigne_xof", nullable = false)
    private long montantConsigneXof;

    @Column(name = "montant_tva_xof", nullable = false)
    private long montantTvaXof;

    @Column(name = "montant_total_xof", nullable = false)
    private long montantTotalXof;

    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    @Column(name = "date_heure", nullable = false)
    private Instant dateHeure;

    @OneToMany(mappedBy = "vente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneVente> lignes = new ArrayList<>();

    protected Vente() {
    }

    private Vente(SessionVente sessionVente, Client client, String numeroBonCommande, String numeroFacture,
                   ModePaiement modePaiement, String idempotencyKey, Instant maintenant) {
        this.sessionVente = sessionVente;
        this.client = client;
        this.numeroBonCommande = numeroBonCommande;
        this.numeroFacture = numeroFacture;
        this.modePaiement = modePaiement;
        this.idempotencyKey = idempotencyKey;
        this.dateHeure = maintenant;
    }

    /**
     * §8.2 étape 6 — « Commander ». RG-23 : rejetée hors session ouverte.
     * {@code tauxTvaPourcent} (§20.5) s'applique au seul sous-total produit,
     * jamais à la consigne (§20.2 : simple montant facturé, non taxé).
     */
    public static Vente commander(SessionVente sessionVente, Client client, String numeroBonCommande,
                                   String numeroFacture, ModePaiement modePaiement,
                                   List<LigneVenteDemandee> lignesDemandees, int tauxTvaPourcent,
                                   String idempotencyKey, Instant maintenant) {
        sessionVente.exigerOuverte();
        if (lignesDemandees.isEmpty()) {
            throw new VenteInvalideException("Le panier est vide.");
        }
        Vente vente = new Vente(sessionVente, client, numeroBonCommande, numeroFacture, modePaiement,
                idempotencyKey, maintenant);
        for (LigneVenteDemandee demandee : lignesDemandees) {
            vente.lignes.add(new LigneVente(vente, demandee.produit(), demandee.quantiteDemiCasiers(),
                    demandee.prixVenteCasier(), demandee.montantConsigneCasier()));
        }
        vente.recalculerMontants(tauxTvaPourcent);
        return vente;
    }

    /** RG-29 — corrige la quantité d'une ligne (session en modification) et recalcule les montants. */
    public void modifierQuantiteLigne(Long ligneId, long quantiteDemiCasiers, int tauxTvaPourcent) {
        sessionVente.exigerEnModification();
        ligneParId(ligneId).modifierQuantite(quantiteDemiCasiers);
        recalculerMontants(tauxTvaPourcent);
    }

    private void recalculerMontants(int tauxTvaPourcent) {
        Montant sousTotal = lignes.stream().map(LigneVente::montantLigne).reduce(Montant.zero(), Montant::plus);
        Montant consigne = lignes.stream().map(LigneVente::montantConsigneLigne).reduce(Montant.zero(), Montant::plus);
        Montant tva = sousTotal.pourcentage(tauxTvaPourcent);
        this.montantSousTotalXof = sousTotal.valeurXof();
        this.montantConsigneXof = consigne.valeurXof();
        this.montantTvaXof = tva.valeurXof();
        this.montantTotalXof = sousTotal.plus(tva).plus(consigne).valeurXof();
    }

    private LigneVente ligneParId(Long ligneId) {
        return lignes.stream()
                .filter(ligne -> ligne.getId() != null && ligne.getId().equals(ligneId))
                .findFirst()
                .orElseThrow(() -> new VenteInvalideException("Ligne introuvable : " + ligneId));
    }

    public Long getId() {
        return id;
    }

    public SessionVente getSessionVente() {
        return sessionVente;
    }

    public Client getClient() {
        return client;
    }

    public String getNumeroBonCommande() {
        return numeroBonCommande;
    }

    public String getNumeroFacture() {
        return numeroFacture;
    }

    public ModePaiement getModePaiement() {
        return modePaiement;
    }

    public Montant getMontantSousTotal() {
        return new Montant(montantSousTotalXof);
    }

    public Montant getMontantConsigne() {
        return new Montant(montantConsigneXof);
    }

    public Montant getMontantTva() {
        return new Montant(montantTvaXof);
    }

    public Montant getMontantTotal() {
        return new Montant(montantTotalXof);
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public Instant getDateHeure() {
        return dateHeure;
    }

    public List<LigneVente> getLignes() {
        return List.copyOf(lignes);
    }
}
