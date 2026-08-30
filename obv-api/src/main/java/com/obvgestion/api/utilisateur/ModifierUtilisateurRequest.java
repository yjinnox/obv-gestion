package com.obvgestion.api.utilisateur;

import jakarta.validation.constraints.NotBlank;

public record ModifierUtilisateurRequest(@NotBlank String nom, @NotBlank String prenoms) {
}
