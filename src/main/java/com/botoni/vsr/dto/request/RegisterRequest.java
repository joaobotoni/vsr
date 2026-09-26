package com.botoni.vsr.dto.request;

import com.botoni.vsr.vo.Cpf;
import com.botoni.vsr.vo.Email;
import com.botoni.vsr.vo.Password;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public record RegisterRequest(
        @NotBlank @Size(max = 200) String name,
        @NotNull Cpf cpf,
        @NotNull Email email,
        @NotNull Password password
) {
}