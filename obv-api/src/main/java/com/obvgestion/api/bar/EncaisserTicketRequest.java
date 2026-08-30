package com.obvgestion.api.bar;

import com.obvgestion.domain.vente.ModePaiement;
import jakarta.validation.constraints.NotNull;

public record EncaisserTicketRequest(@NotNull ModePaiement modePaiement) {
}
