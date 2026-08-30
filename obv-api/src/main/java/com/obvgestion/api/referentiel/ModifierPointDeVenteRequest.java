package com.obvgestion.api.referentiel;

import jakarta.validation.constraints.NotBlank;

public record ModifierPointDeVenteRequest(@NotBlank String libelle, String adresse, boolean actif) {
}
