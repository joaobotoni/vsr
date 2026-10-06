package com.botoni.vsr.exception.enums.problem;

import com.botoni.vsr.exception.lib.problem.Problem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum PasswordHashProblem implements Problem {

    MISSING(HttpStatus.INTERNAL_SERVER_ERROR, "O hash da senha é obrigatório."),
    TOO_SHORT(HttpStatus.INTERNAL_SERVER_ERROR, "O hash da senha deve conter no mínimo %d caracteres."),
    TOO_LONG(HttpStatus.INTERNAL_SERVER_ERROR, "O hash da senha deve conter no máximo %d caracteres."),
    CONTAINS_WHITESPACE(HttpStatus.INTERNAL_SERVER_ERROR, "O hash da senha não pode conter espaços em branco."),
    INVALID_CHARACTERS(HttpStatus.INTERNAL_SERVER_ERROR, "O hash da senha contém caracteres inválidos.");

    private final HttpStatus status;
    private final String message;
}
