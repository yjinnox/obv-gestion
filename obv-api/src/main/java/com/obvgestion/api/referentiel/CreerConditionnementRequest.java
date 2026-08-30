package com.obvgestion.api.referentiel;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreerConditionnementRequest(@NotNull Long produitId, @Positive int capaciteBouteilles) {
}
