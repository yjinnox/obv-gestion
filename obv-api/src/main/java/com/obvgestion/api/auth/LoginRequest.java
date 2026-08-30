package com.obvgestion.api.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String identifiant, @NotBlank String motDePasse) {
}
