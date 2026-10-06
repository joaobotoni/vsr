package com.botoni.vsr.dto.response;

public record AuthenticationResponse(
        UserResponse user,
        TokenResponse token
) {
}
