package com.botoni.vsr.exception.enums.problem;

import com.botoni.vsr.exception.lib.problem.Problem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum PasswordProblem implements Problem {

    MISSING(HttpStatus.BAD_REQUEST, "A senha é obrigatória."),
    TOO_SHORT(HttpStatus.BAD_REQUEST, "A senha deve conter no mínimo %d caracteres."),
    TOO_LONG(HttpStatus.BAD_REQUEST, "A senha excede o tamanho máximo permitido."),
    CONTAINS_WHITESPACE(HttpStatus.BAD_REQUEST, "A senha não pode conter espaços em branco."),
    MISSING_UPPERCASE(HttpStatus.BAD_REQUEST, "A senha deve conter ao menos uma letra maiúscula."),
    MISSING_LOWERCASE(HttpStatus.BAD_REQUEST, "A senha deve conter ao menos uma letra minúscula."),
    MISSING_DIGIT(HttpStatus.BAD_REQUEST, "A senha deve conter ao menos um dígito numérico."),
    MISSING_SPECIAL_CHARACTER(HttpStatus.BAD_REQUEST, "A senha deve conter ao menos um caractere especial.");

    private final HttpStatus status;
    private final String message;
}
