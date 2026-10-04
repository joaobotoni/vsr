package com.botoni.vsr.dto.request.auth;

import com.botoni.vsr.dto.request.session.DeviceRequest;
import com.botoni.vsr.vo.Email;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(
        @NotNull(message = "O e-mail é obrigatório.")
        Email email,

        @NotBlank(message = "A senha é obrigatória.")
        String password,

        @Valid
        @NotNull(message = "Os dados do dispositivo são obrigatórios.")
        DeviceRequest device
) {}