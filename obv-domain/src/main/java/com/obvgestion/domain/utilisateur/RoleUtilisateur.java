package com.obvgestion.domain.utilisateur;

import com.obvgestion.domain.referentiel.TypePointDeVente;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import static com.obvgestion.domain.utilisateur.Permission.*;

/**
 * Rôles applicatifs (§3.1) et permissions associées (§3.3).
 *
 * <p>La matrice §3.3 ne couvre pas explicitement UTILISATEUR_READ,
 * RECEPTION_READ, VENTE_READ, CLIENT_READ et CLIENT_WRITE : par décision
 * produit, ces permissions restent réservées aux rôles à portée globale
 * (SUPER_ADMINISTRATEUR, ADMINISTRATEUR). TRANSFERT_VALIDER reste réservé
 * au seul SUPER_ADMINISTRATEUR conformément à RG-01, qui l'exclut donc
 * explicitement d'ADMINISTRATEUR malgré sa portée globale.
 */
public enum RoleUtilisateur {

    SUPER_ADMINISTRATEUR(Portee.GLOBALE, null, EnumSet.allOf(Permission.class)),

    ADMINISTRATEUR(Portee.GLOBALE, null, EnumSet.of(
            REFERENTIEL_READ, REFERENTIEL_WRITE,
            UTILISATEUR_READ, UTILISATEUR_WRITE,
            RECEPTION_READ, VENTE_READ,
            CLIENT_READ, CLIENT_WRITE,
            RAPPORT_READ)),

    GERANT_DEPOT(Portee.POINT_DE_VENTE, TypePointDeVente.DEPOT, EnumSet.of(
            REFERENTIEL_READ, RECEPTION_WRITE, VENTE_WRITE,
            SESSION_CLOTURER, TRANSFERT_WRITE, RAPPORT_READ)),

    GERANT_BAR(Portee.POINT_DE_VENTE, TypePointDeVente.BAR, EnumSet.of(
            REFERENTIEL_READ, VENTE_WRITE,
            SESSION_CLOTURER, TRANSFERT_WRITE, RAPPORT_READ)),

    VENDEUR(Portee.POINT_DE_VENTE, null, EnumSet.of(
            REFERENTIEL_READ, VENTE_WRITE));

    /** Portée d'application d'un rôle (§3.1). */
    public enum Portee {
        /** S'applique à toute l'organisation, sans point de vente associé. */
        GLOBALE,
        /** S'applique à un unique point de vente rattaché à l'utilisateur. */
        POINT_DE_VENTE
    }

    private final Portee portee;
    private final TypePointDeVente typePointDeVenteRequis;
    private final Set<Permission> permissions;

    RoleUtilisateur(Portee portee, TypePointDeVente typePointDeVenteRequis, Set<Permission> permissions) {
        this.portee = portee;
        this.typePointDeVenteRequis = typePointDeVenteRequis;
        this.permissions = Collections.unmodifiableSet(permissions);
    }

    public Portee portee() {
        return portee;
    }

    /**
     * Type de point de vente imposé pour ce rôle, ou {@code null} si le
     * rôle est global (aucun point de vente) ou accepte tout type de point
     * de vente (cas de VENDEUR, §3.1).
     */
    public TypePointDeVente typePointDeVenteRequis() {
        return typePointDeVenteRequis;
    }

    public Set<Permission> permissions() {
        return permissions;
    }
}
