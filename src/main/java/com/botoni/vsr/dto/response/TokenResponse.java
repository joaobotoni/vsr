package com.botoni.vsr.dto.response;

public record TokenResponse(
        String accessToken,
        long expiresIn
) {
}