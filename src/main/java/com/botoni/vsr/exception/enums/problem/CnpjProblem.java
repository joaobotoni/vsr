package com.botoni.vsr.exception.enums.problem;

import com.botoni.vsr.exception.lib.problem.Problem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum CnpjProblem implements Problem {

    MISSING(HttpStatus.BAD_REQUEST, "O CNPJ é obrigatório."),
    LENGTH(HttpStatus.BAD_REQUEST, "O CNPJ deve conter exatamente %d caracteres."),
    CHARACTERS(HttpStatus.BAD_REQUEST, "O CNPJ deve conter apenas letras maiúsculas e números."),
    NON_NUMERIC_CHECK_DIGITS(HttpStatus.BAD_REQUEST, "Os dígitos verificadores do CNPJ devem ser numéricos."),
    REPEATED_CHARACTERS(HttpStatus.BAD_REQUEST, "O CNPJ informado é inválido, pois todos os caracteres são iguais."),
    CHECK_DIGITS(HttpStatus.BAD_REQUEST, "O CNPJ informado é inválido, pois os dígitos verificadores não conferem.");

    private final HttpStatus status;
    private final String message;
}
