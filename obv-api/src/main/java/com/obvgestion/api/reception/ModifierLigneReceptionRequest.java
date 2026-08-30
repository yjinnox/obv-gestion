package com.obvgestion.api.reception;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ModifierLigneReceptionRequest(@Positive long nombreCasiers, @NotNull Long prixAchatCasierXof) {
}
