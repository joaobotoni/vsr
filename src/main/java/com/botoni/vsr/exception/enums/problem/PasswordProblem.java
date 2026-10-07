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
    TOO_LONG(HttpStatus.BAD_REQUEST, "A senha deve conter no máximo %d caracteres.");

    private final HttpStatus status;
    private final String message;
}
