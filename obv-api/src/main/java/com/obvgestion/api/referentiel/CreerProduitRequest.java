package com.obvgestion.api.referentiel;

import jakarta.validation.constraints.NotNull;

public record CreerProduitRequest(@NotNull Long marqueId, @NotNull Long volumeId) {
}
