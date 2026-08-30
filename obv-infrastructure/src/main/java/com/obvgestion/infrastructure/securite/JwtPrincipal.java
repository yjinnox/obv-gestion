package com.obvgestion.infrastructure.securite;

import com.obvgestion.domain.utilisateur.Permission;

import java.util.Set;

/** Identité et permissions portées par un jeton d'accès valide (§4.4). */
public record JwtPrincipal(Long utilisateurId, Set<Permission> permissions) {

    public JwtPrincipal {
        permissions = Set.copyOf(permissions);
    }
}
