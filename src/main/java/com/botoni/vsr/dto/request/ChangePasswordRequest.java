package com.botoni.vsr.dto.request;

import com.botoni.vsr.vo.Password;
import jakarta.validation.constraints.NotNull;

public record ChangePasswordRequest(
        @NotNull Password currentPassword,
        @NotNull Password newPassword
) {
}
