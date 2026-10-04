package com.botoni.vsr.dto.request.password;

import com.botoni.vsr.vo.Password;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChangePasswordRequest(
        @NotBlank(message = "A senha atual é obrigatória.") String currentPassword,
        @NotNull(message = "A nova senha é obrigatória.") Password newPassword
) {
}
