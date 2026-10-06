package com.botoni.vsr.exception.enums.problem;

import com.botoni.vsr.exception.lib.problem.Problem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum CpfProblem implements Problem {

    MISSING(HttpStatus.BAD_REQUEST, "O CPF é obrigatório."),
    LENGTH(HttpStatus.BAD_REQUEST, "O CPF deve conter exatamente %d dígitos."),
    CHARACTERS(HttpStatus.BAD_REQUEST, "O CPF deve conter apenas dígitos numéricos."),
    REPEATED_DIGITS(HttpStatus.BAD_REQUEST, "O CPF informado é inválido, pois todos os dígitos são iguais."),
    CHECK_DIGITS(HttpStatus.BAD_REQUEST, "O CPF informado é inválido, pois os dígitos verificadores não conferem.");

    private final HttpStatus status;
    private final String message;
}
