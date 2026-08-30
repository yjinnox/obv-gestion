package com.obvgestion.api.referentiel;

import jakarta.validation.constraints.NotBlank;

public record CreerMarqueRequest(@NotBlank String libelle) {
}
