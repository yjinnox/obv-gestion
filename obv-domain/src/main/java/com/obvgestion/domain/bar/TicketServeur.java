package com.obvgestion.domain.bar;

import com.obvgestion.domain.audit.Auditable;
import com.obvgestion.domain.commun.Montant;
import com.obvgestion.domain.referentiel.Produit;
import com.obvgestion.domain.referentiel.Serveur;
import com.obvgestion.domain.vente.ModePaiement;
import com.obvgestion.domain.vente.SessionVente;
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
import java.util.Objects;

/**
 * Ligne de vente par serveur au bar/maquis (§10) : le serveur commande
 * auprès du gérant, qui encaisse et remet les bouteilles. Réutilise
 * {@link SessionVente} (RG-23, RG-34 : même schéma qu'au dépôt) — pas de
 * facture ni de sélection de client, vente au comptoir (§10).
 */
@Entity
@Table(name = "ticket_serveur")
@Audited
public class TicketServeur extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_vente_id", nullable = false)
    private SessionVente sessionVente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "serveur_id", nullable = false)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Serveur serveur;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutTicketServeur statut = StatutTicketServeur.OUVERT;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode_paiement", length = 20)
    private ModePaiement modePaiement;

    @Column(name = "montant_total_xof", nullable = false)
    private long montantTotalXof;

    @Column(name = "date_encaissement")
    private Instant dateEncaissement;

    @Column(name = "encaissee_par")
    private String encaisseePar;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneTicketServeur> lignes = new ArrayList<>();

    protected TicketServeur() {
    }

    /** RG-23 — aucun ticket ne peut être ouvert hors session ouverte ; le serveur doit appartenir au même point de vente. */
    public TicketServeur(SessionVente sessionVente, Serveur serveur) {
        sessionVente.exigerOuverte();
        if (!Objects.equals(serveur.getPointDeVente(), sessionVente.getPointDeVente())) {
            throw new TicketServeurInvalideException(
                    "Le serveur doit appartenir au point de vente de la session.");
        }
        this.sessionVente = sessionVente;
        this.serveur = serveur;
    }

    public LigneTicketServeur ajouterLigne(Produit produit, long quantiteBouteilles, Montant prixVenteBouteille) {
        exigerOuvert();
        LigneTicketServeur ligne = new LigneTicketServeur(this, produit, quantiteBouteilles, prixVenteBouteille);
        lignes.add(ligne);
        return ligne;
    }

    /** §10 — le gérant encaisse et remet les bouteilles ; le décrément de stock est appliqué par l'appelant (RG-24 par analogie). */
    public void encaisser(ModePaiement modePaiement, String encaisseePar, Instant maintenant) {
        exigerOuvert();
        sessionVente.exigerOuverte();
        if (lignes.isEmpty()) {
            throw new TicketServeurInvalideException("Un ticket sans ligne ne peut pas être encaissé.");
        }
        this.statut = StatutTicketServeur.ENCAISSE;
        this.modePaiement = modePaiement;
        this.dateEncaissement = maintenant;
        this.encaisseePar = encaisseePar;
        recalculerMontant();
    }

    /** RG-29 (par analogie) — corrige la quantité d'une ligne pendant la modification de session et recalcule le total. */
    public void modifierQuantiteLigne(Long ligneId, long quantiteBouteilles) {
        sessionVente.exigerEnModification();
        ligneParId(ligneId).modifierQuantite(quantiteBouteilles);
        recalculerMontant();
    }

    private void recalculerMontant() {
        this.montantTotalXof = lignes.stream()
                .map(LigneTicketServeur::montantLigne)
                .reduce(Montant.zero(), Montant::plus)
                .valeurXof();
    }

    private void exigerOuvert() {
        if (statut != StatutTicketServeur.OUVERT) {
            throw new TicketServeurInvalideException("Ce ticket ne peut plus être modifié (statut " + statut + ").");
        }
    }

    private LigneTicketServeur ligneParId(Long ligneId) {
        return lignes.stream()
                .filter(ligne -> ligne.getId() != null && ligne.getId().equals(ligneId))
                .findFirst()
                .orElseThrow(() -> new TicketServeurInvalideException("Ligne introuvable : " + ligneId));
    }

    public Long getId() {
        return id;
    }

    public SessionVente getSessionVente() {
        return sessionVente;
    }

    public Serveur getServeur() {
        return serveur;
    }

    public StatutTicketServeur getStatut() {
        return statut;
    }

    public ModePaiement getModePaiement() {
        return modePaiement;
    }

    public Montant getMontantTotal() {
        return new Montant(montantTotalXof);
    }

    public Instant getDateEncaissement() {
        return dateEncaissement;
    }

    public String getEncaisseePar() {
        return encaisseePar;
    }

    public List<LigneTicketServeur> getLignes() {
        return List.copyOf(lignes);
    }
}
