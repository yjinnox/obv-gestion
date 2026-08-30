package com.obvgestion.api.referentiel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record ModifierVolumeRequest(@NotBlank String libelle, @Positive int contenanceMl, boolean actif) {
}
