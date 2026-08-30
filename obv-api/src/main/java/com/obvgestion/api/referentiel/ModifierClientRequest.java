package com.obvgestion.api.referentiel;

import jakarta.validation.constraints.NotBlank;

public record ModifierClientRequest(@NotBlank String telephone, String email, String adresseFacturation,
                                     boolean actif) {
}
