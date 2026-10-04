package com.botoni.vsr.auth.dto.request;

import com.botoni.vsr.shared.vo.Cpf;
import com.botoni.vsr.session.dto.request.DeviceRequest;
import com.botoni.vsr.shared.vo.Email;
import com.botoni.vsr.shared.vo.Password;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public record RegisterRequest(
        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 200, message = "O nome deve conter no máximo {max} caracteres.")
        String name,

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
