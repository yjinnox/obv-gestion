package com.obvgestion.api.referentiel;

import jakarta.validation.constraints.NotBlank;

public record ModifierMarqueRequest(@NotBlank String libelle, boolean actif) {
}
