package com.botoni.vsr.exception.enums.problem;

import com.botoni.vsr.exception.lib.problem.Problem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum UnexpectedProblem implements Problem {

    UNEXPECTED_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro inesperado."),
    REQUEST_FAILED(HttpStatus.BAD_REQUEST, "Não foi possível processar a requisição.");

    private final HttpStatus status;
    private final String message;
}
