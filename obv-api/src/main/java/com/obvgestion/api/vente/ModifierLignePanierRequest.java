package com.obvgestion.api.vente;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ModifierLignePanierRequest(@NotNull Long sessionVenteId, @Positive long quantiteDemiCasiers) {
}
