package com.obvgestion.api.referentiel;

import com.obvgestion.domain.referentiel.TypePointDeVente;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreerPointDeVenteRequest(@NotBlank String libelle, @NotNull TypePointDeVente type, String adresse) {
}
