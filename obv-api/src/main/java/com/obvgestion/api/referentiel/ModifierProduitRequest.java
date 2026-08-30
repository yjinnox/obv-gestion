package com.obvgestion.api.referentiel;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ModifierProduitRequest(@NotNull @PositiveOrZero Long montantConsigneXof, boolean actif) {
}
