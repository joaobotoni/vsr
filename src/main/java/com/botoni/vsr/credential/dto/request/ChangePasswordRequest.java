package com.botoni.vsr.credential.dto.request;

import com.botoni.vsr.shared.vo.Password;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChangePasswordRequest(
        @NotBlank(message = "A senha atual é obrigatória.") String currentPassword,
        @NotNull(message = "A nova senha é obrigatória.") Password newPassword
) {
}
