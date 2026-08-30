package com.obvgestion.application.utilisateur;

import java.util.Optional;

/** Port §4.4 — refresh token rotatif, révocable, TTL 7 jours. */
public interface GestionnaireRefreshToken {

    String emettre(Long utilisateurId);

    /** Consomme (rotation) le jeton fourni et retourne l'utilisateur associé, s'il est valide. */
    Optional<Long> consommer(String tokenClair);

    void revoquer(String tokenClair);
}
