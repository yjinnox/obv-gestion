package com.obvgestion.api.bar;

import jakarta.validation.constraints.Positive;

public record ModifierLigneTicketRequest(@Positive long quantiteBouteilles) {
}
