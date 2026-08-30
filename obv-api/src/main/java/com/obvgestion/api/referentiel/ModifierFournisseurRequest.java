package com.obvgestion.api.referentiel;

import jakarta.validation.constraints.NotBlank;

public record ModifierFournisseurRequest(@NotBlank String raisonSociale, String telephone, String email,
                                          String adresse, boolean actif) {
}
