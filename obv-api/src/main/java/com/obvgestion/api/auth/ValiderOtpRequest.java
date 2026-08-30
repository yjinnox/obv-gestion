package com.obvgestion.api.auth;

import jakarta.validation.constraints.NotBlank;

public record ValiderOtpRequest(@NotBlank String code) {
}
