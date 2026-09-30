package com.botoni.vsr.dto.response;

public record RegisterResponse(
        UserResponse user,
        TokenResponse token
) {
}