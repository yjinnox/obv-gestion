package com.obvgestion.api.referentiel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreerServeurRequest(@NotNull Long pointDeVenteId, @NotBlank String nom, @NotBlank String prenoms,
                                   String telephone) {
}
