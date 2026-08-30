package com.obvgestion.api.transfert;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

public record CreerBonTransfertRequest(@NotNull Long pointDeVenteSourceId, @NotNull Long pointDeVenteDestinationId,
                                        @NotNull Instant dateHeure,
                                        @NotEmpty List<@Valid LigneTransfertRequest> lignes) {

    public CreerBonTransfertRequest {
        lignes = lignes == null ? null : List.copyOf(lignes);
    }
}
