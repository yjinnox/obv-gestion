package com.obvgestion.api.auth;

import jakarta.validation.constraints.NotBlank;

public record DefinirMotDePasseRequest(@NotBlank String motDePasse, @NotBlank String confirmation) {
}
