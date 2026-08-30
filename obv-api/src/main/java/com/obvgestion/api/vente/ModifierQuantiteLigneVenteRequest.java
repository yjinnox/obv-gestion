package com.obvgestion.api.vente;

import jakarta.validation.constraints.Positive;

public record ModifierQuantiteLigneVenteRequest(@Positive long quantiteDemiCasiers) {
}
