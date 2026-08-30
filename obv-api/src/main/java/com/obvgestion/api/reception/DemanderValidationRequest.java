package com.obvgestion.api.reception;

import jakarta.validation.constraints.NotNull;

public record DemanderValidationRequest(@NotNull Long destinataireId) {
}
