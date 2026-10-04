package com.botoni.vsr.auth.dto.response;

import com.botoni.vsr.token.dto.response.TokenResponse;
import com.botoni.vsr.user.dto.response.UserResponse;

public record LoginResponse(
        UserResponse user,
        TokenResponse token
) {
}
