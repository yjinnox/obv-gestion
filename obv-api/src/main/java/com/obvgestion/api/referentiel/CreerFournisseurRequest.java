package com.obvgestion.api.referentiel;

import jakarta.validation.constraints.NotBlank;

public record CreerFournisseurRequest(@NotBlank String raisonSociale, String telephone, String email,
                                       String adresse) {
}
