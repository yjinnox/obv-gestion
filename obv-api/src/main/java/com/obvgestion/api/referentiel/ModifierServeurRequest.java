package com.obvgestion.api.referentiel;

import jakarta.validation.constraints.NotBlank;

public record ModifierServeurRequest(@NotBlank String nom, @NotBlank String prenoms, String telephone,
                                      boolean actif) {
}
