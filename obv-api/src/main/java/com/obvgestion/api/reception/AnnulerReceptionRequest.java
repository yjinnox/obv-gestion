package com.obvgestion.api.reception;

import jakarta.validation.constraints.NotBlank;

public record AnnulerReceptionRequest(@NotBlank String motif) {
}
