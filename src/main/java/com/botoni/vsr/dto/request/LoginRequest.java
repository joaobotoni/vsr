package com.botoni.vsr.dto.request;

import com.botoni.vsr.vo.Email;
import com.botoni.vsr.vo.Password;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(
        @NotNull Email email,
        @NotNull Password password
) {
}