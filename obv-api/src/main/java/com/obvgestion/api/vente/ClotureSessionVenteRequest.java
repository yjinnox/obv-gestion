package com.obvgestion.api.vente;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ClotureSessionVenteRequest(@NotNull @PositiveOrZero Long totalCompteXof) {
}
