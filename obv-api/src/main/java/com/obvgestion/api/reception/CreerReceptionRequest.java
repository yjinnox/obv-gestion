package com.obvgestion.api.reception;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record CreerReceptionRequest(@NotNull Long fournisseurId, @NotNull Long pointDeVenteId,
                                     @NotNull Instant dateHeureLivraison) {
}
