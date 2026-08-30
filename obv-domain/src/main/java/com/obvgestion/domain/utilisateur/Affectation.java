package com.obvgestion.domain.utilisateur;

import com.obvgestion.domain.audit.Auditable;
import com.obvgestion.domain.referentiel.PointDeVente;
import com.obvgestion.domain.referentiel.TypePointDeVente;
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

/**
 * Rattachement d'un rôle à un utilisateur, éventuellement scopé à un point
 * de vente (H10 : « un utilisateur est rattaché à un ou plusieurs points de
 * vente ; ses droits s'appliquent par point de vente »).
 */
@Entity
@Table(name = "affectation")
public class Affectation extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RoleUtilisateur role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "point_de_vente_id")
    private PointDeVente pointDeVente;

    protected Affectation() {
    }

    private Affectation(Utilisateur utilisateur, RoleUtilisateur role, PointDeVente pointDeVente) {
        this.utilisateur = utilisateur;
        this.role = role;
        this.pointDeVente = pointDeVente;
    }

    /**
     * Construit une affectation en validant la cohérence rôle / point de
     * vente (§3.1) : un rôle global (SUPER_ADMINISTRATEUR, ADMINISTRATEUR)
     * n'a aucun point de vente ; un rôle scopé en requiert un, du type
     * imposé par le rôle le cas échéant (GERANT_DEPOT → DEPOT,
     * GERANT_BAR → BAR).
     */
    public static Affectation of(Utilisateur utilisateur, RoleUtilisateur role, PointDeVente pointDeVente) {
        valider(role, pointDeVente);
        return new Affectation(utilisateur, role, pointDeVente);
    }

    private static void valider(RoleUtilisateur role, PointDeVente pointDeVente) {
        if (role.portee() == RoleUtilisateur.Portee.GLOBALE) {
            if (pointDeVente != null) {
                throw new AffectationInvalideException(
                        "Le rôle " + role + " est global : aucun point de vente ne doit être renseigné.");
            }
            return;
        }
        if (pointDeVente == null) {
            throw new AffectationInvalideException("Le rôle " + role + " requiert un point de vente.");
        }
        TypePointDeVente typeRequis = role.typePointDeVenteRequis();
        if (typeRequis != null && pointDeVente.getType() != typeRequis) {
            throw new AffectationInvalideException(
                    "Le rôle " + role + " requiert un point de vente de type " + typeRequis
                            + " (reçu : " + pointDeVente.getType() + ").");
        }
    }

    public Long getId() {
        return id;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public RoleUtilisateur getRole() {
        return role;
    }

    public PointDeVente getPointDeVente() {
        return pointDeVente;
    }
}
