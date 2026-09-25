package com.botoni.vsr.dto.response;

public record LoginResponse(String token, long expiresInMs) {
}
