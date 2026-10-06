package com.botoni.vsr.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        @NotBlank(message = "O refresh token é obrigatório.")
        String refreshToken
) {
}
