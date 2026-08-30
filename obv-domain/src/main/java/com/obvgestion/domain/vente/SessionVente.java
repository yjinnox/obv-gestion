package com.obvgestion.domain.vente;

import com.obvgestion.domain.audit.Auditable;
import com.obvgestion.domain.commun.Montant;
import com.obvgestion.domain.commun.SeparationDesTachesException;
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
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import java.time.Instant;

/**
 * Journée de vente au dépôt (§8.1, ajout majeur : absente de l'expression
 * de besoin initiale). RG-23 : une seule session {@code OUVERTE} par point
 * de vente à la fois (garanti en base par un index unique partiel, comme
 * {@code uk_tarif_ouvert}).
 */
@Entity
@Table(name = "session_vente")
@Audited
public class SessionVente extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "point_de_vente_id", nullable = false)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private PointDeVente pointDeVente;

    @Column(name = "date_ouverture", nullable = false)
    private Instant dateOuverture;

    @Column(name = "ouverte_par", nullable = false)
    private String ouvertePar;

    @Column(name = "fond_caisse_xof", nullable = false)
    private long fondCaisseXof;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutSessionVente statut = StatutSessionVente.OUVERTE;

    @Column(name = "date_cloture")
    private Instant dateCloture;

    @Column(name = "cloturee_par")
    private String clotureePar;

    @Column(name = "total_theorique_xof")
    private Long totalTheoriqueXof;

    @Column(name = "total_compte_xof")
    private Long totalCompteXof;

    /** Positif = surplus de caisse, négatif = manquant. Jamais un {@link Montant} : peut être négatif. */
    @Column(name = "ecart_xof")
    private Long ecartXof;

    @Column(name = "date_validation")
    private Instant dateValidation;

    @Column(name = "validee_par")
    private String valideePar;

    protected SessionVente() {
    }

    public SessionVente(PointDeVente pointDeVente, String ouvertePar, Montant fondCaisse, Instant maintenant) {
        this.pointDeVente = pointDeVente;
        this.ouvertePar = ouvertePar;
        this.fondCaisseXof = fondCaisse.valeurXof();
        this.dateOuverture = maintenant;
    }

    /** §8.3 — récapitule et clôt ; {@code totalTheorique} est calculé par l'appelant (somme des ventes). */
    public void cloturer(String clotureePar, Montant totalTheorique, Montant totalCompte, Instant maintenant) {
        if (statut != StatutSessionVente.OUVERTE) {
            throw new SessionVenteInvalideException("Seule une session ouverte peut être clôturée.");
        }
        this.statut = StatutSessionVente.CLOTUREE;
        this.dateCloture = maintenant;
        this.clotureePar = clotureePar;
        this.totalTheoriqueXof = totalTheorique.valeurXof();
        this.totalCompteXof = totalCompte.valeurXof();
        this.ecartXof = totalCompte.valeurXof() - totalTheorique.valeurXof();
    }

    /** RG-01/RG-28 — un utilisateur ne peut jamais valider une session qu'il a lui-même clôturée. */
    public void valider(String valideePar, Instant maintenant) {
        if (statut != StatutSessionVente.CLOTUREE && statut != StatutSessionVente.EN_MODIFICATION) {
            throw new SessionVenteInvalideException(
                    "Cette action nécessite une session clôturée ou en modification (statut actuel : " + statut + ").");
        }
        if (valideePar.equals(clotureePar)) {
            throw new SeparationDesTachesException(
                    "Vous ne pouvez pas valider une session que vous avez vous-même clôturée.");
        }
        this.statut = StatutSessionVente.VALIDEE;
        this.dateValidation = maintenant;
        this.valideePar = valideePar;
    }

    /** RG-29 — bascule en modification ; la notification au SUPER_ADMIN est envoyée par l'appelant. */
    public void demanderModification() {
        if (statut != StatutSessionVente.CLOTUREE) {
            throw new SessionVenteInvalideException("Seule une session clôturée peut faire l'objet d'une demande de modification.");
        }
        this.statut = StatutSessionVente.EN_MODIFICATION;
    }

    /** RG-29 — seule une session en modification autorise la correction de ses ventes. */
    public void exigerEnModification() {
        if (statut != StatutSessionVente.EN_MODIFICATION) {
            throw new SessionVenteInvalideException(
                    "Cette session n'est pas en modification (statut actuel : " + statut + ").");
        }
    }

    /** RG-23 — aucune vente ne peut être créée hors session ouverte. */
    public void exigerOuverte() {
        if (statut != StatutSessionVente.OUVERTE) {
            throw new SessionVenteInvalideException(
                    "Aucune vente ne peut être créée hors session ouverte (statut actuel : " + statut + ").");
        }
    }

    public Long getId() {
        return id;
    }

    public PointDeVente getPointDeVente() {
        return pointDeVente;
    }

    public Instant getDateOuverture() {
        return dateOuverture;
    }

    public String getOuvertePar() {
        return ouvertePar;
    }

    public Montant getFondCaisse() {
        return new Montant(fondCaisseXof);
    }

    public StatutSessionVente getStatut() {
        return statut;
    }

    public Instant getDateCloture() {
        return dateCloture;
    }

    public String getClotureePar() {
        return clotureePar;
    }

    public Montant getTotalTheorique() {
        return totalTheoriqueXof == null ? null : new Montant(totalTheoriqueXof);
    }

    public Montant getTotalCompte() {
        return totalCompteXof == null ? null : new Montant(totalCompteXof);
    }

    public Long getEcartXof() {
        return ecartXof;
    }

    public Instant getDateValidation() {
        return dateValidation;
    }

    public String getValideePar() {
        return valideePar;
    }
}
