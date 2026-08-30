package com.obvgestion.api.auth;

import java.util.Set;

public record ConnexionResponse(String accessToken, String refreshToken, Long utilisateurId,
                                 Set<String> permissions) {

    public ConnexionResponse {
        permissions = Set.copyOf(permissions);
    }
}
