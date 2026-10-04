package com.botoni.vsr.auth.dto.request;

import com.botoni.vsr.session.dto.request.DeviceRequest;
import com.botoni.vsr.shared.vo.Email;
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