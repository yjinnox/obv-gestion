package com.obvgestion.application.utilisateur;

import com.obvgestion.domain.utilisateur.Permission;

import java.util.Set;

/** Port d'émission du jeton d'accès JWT (§4.4). */
public interface EmetteurAccessToken {

    String genererAccessToken(Long utilisateurId, Set<Permission> permissions);
}
