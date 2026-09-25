package com.botoni.vsr.dto.request;

import com.botoni.vsr.vo.Password;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(max = 200) String name,
        @NotBlank String cpf,
        @NotBlank @Email @Size(max = 254) String email,
        @JsonSetter(nulls = Nulls.FAIL) Password password
) {
}
