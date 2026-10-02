package com.botoni.vsr.exception.handler.problems;

import com.botoni.vsr.exception.lib.problem.Problem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum ValidationProblem implements Problem {

    INVALID_DATA(HttpStatus.BAD_REQUEST, "Os dados informados são inválidos."),
    UNREADABLE_BODY(HttpStatus.BAD_REQUEST, "O corpo da requisição é inválido.");

    private final HttpStatus status;
    private final String message;
}
