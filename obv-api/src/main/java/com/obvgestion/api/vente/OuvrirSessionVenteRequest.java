package com.obvgestion.api.vente;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record OuvrirSessionVenteRequest(@NotNull Long pointDeVenteId, @NotNull @PositiveOrZero Long fondCaisseXof) {
}
