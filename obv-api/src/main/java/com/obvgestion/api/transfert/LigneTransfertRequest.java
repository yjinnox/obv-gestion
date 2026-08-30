package com.obvgestion.api.transfert;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** {@code prixCessionCasierXof} nullable : pré-rempli depuis le tarif CESSION en vigueur si omis (§9). */
public record LigneTransfertRequest(@NotNull Long produitId, @NotNull Long conditionnementId,
                                     @Positive long quantiteDemiCasiers, Long prixCessionCasierXof) {
}
