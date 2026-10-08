package com.botoni.vsr.dto.request;

import com.botoni.vsr.vo.Cpf;
import com.botoni.vsr.vo.Email;
import com.botoni.vsr.vo.Name;
import com.botoni.vsr.vo.Password;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record RegisterRequest(
        @NotNull(message = "O nome é obrigatório.")
        Name name,

        @NotNull(message = "O CPF é obrigatório.")
        Cpf cpf,

        @NotNull(message = "O e-mail é obrigatório.")
        Email email,

        @NotNull(message = "A senha é obrigatória.")
        Password password,

        @Valid
        @NotNull(message = "Os dados do dispositivo são obrigatórios.")
        DeviceRequest device
) {
}
