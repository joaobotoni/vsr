package com.botoni.vsr.token.dto.response;

public record TokenResponse(
        String accessToken,
        long expiresIn
) {
}