package com.botoni.vsr.dto.request;

import com.botoni.vsr.vo.Password;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        @NotBlank String currentPassword,
        @JsonSetter(nulls = Nulls.FAIL) Password newPassword
) {
}
