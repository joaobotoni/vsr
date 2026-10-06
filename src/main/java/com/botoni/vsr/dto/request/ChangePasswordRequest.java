package com.botoni.vsr.dto.request;

import com.botoni.vsr.vo.Password;
import jakarta.validation.constraints.NotNull;

public record ChangePasswordRequest(
        @NotNull(message = "A senha atual é obrigatória.")
        Password currentPassword,

        @NotNull(message = "A nova senha é obrigatória.")
        Password newPassword
) {
}
