package com.obvgestion.api.reception;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** {@code prixAchatCasierXof} nullable : pré-rempli depuis le tarif ACHAT en vigueur si omis (§7.2). */
public record AjouterLigneReceptionRequest(@NotNull Long produitId, @NotNull Long conditionnementId,
                                            @Positive long nombreCasiers, Long prixAchatCasierXof) {
}
