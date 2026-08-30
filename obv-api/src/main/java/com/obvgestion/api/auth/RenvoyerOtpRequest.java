package com.obvgestion.api.auth;

import jakarta.validation.constraints.NotBlank;

public record RenvoyerOtpRequest(@NotBlank String token) {
}
