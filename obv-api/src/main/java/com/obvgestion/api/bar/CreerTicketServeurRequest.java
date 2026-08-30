package com.obvgestion.api.bar;

import jakarta.validation.constraints.NotNull;

public record CreerTicketServeurRequest(@NotNull Long sessionVenteId, @NotNull Long serveurId) {
}
