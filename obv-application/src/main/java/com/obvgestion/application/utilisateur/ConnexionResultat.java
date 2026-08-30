package com.obvgestion.application.utilisateur;

import com.obvgestion.domain.utilisateur.Permission;

import java.util.Set;

/** §4.4 — résultat d'une authentification ou d'un rafraîchissement réussi. */
public record ConnexionResultat(String accessToken, String refreshToken, Long utilisateurId,
                                 Set<Permission> permissions) {

    public ConnexionResultat {
        permissions = Set.copyOf(permissions);
    }
}
