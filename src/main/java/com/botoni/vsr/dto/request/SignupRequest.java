package com.botoni.vsr.dto.request;

import com.botoni.vsr.vo.Password;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank @Size(max = SignupRequest.MAX_NAME_LENGTH) String name,
        @NotBlank String cpf,
        @NotBlank @Email @Size(max = SignupRequest.MAX_EMAIL_LENGTH) String email,
        @JsonSetter(nulls = Nulls.FAIL) Password password
) {

    private static final int MAX_NAME_LENGTH = 200;
    private static final int MAX_EMAIL_LENGTH = 254;
}
