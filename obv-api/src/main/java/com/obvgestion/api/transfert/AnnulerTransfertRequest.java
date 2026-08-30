package com.obvgestion.api.transfert;

import jakarta.validation.constraints.NotBlank;

public record AnnulerTransfertRequest(@NotBlank String motif) {
}
