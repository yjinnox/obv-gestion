package com.obvgestion.api.vente;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AjouterLignePanierRequest(@NotNull Long sessionVenteId, @NotNull Long produitId,
                                         @Positive long quantiteDemiCasiers) {
}
