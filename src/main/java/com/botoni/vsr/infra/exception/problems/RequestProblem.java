package com.botoni.vsr.infra.exception.problems;

import com.botoni.vsr.infra.exception.lib.problem.Problem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum RequestProblem implements Problem {

    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "O recurso solicitado não foi encontrado."),
    METHOD_NOT_SUPPORTED(HttpStatus.METHOD_NOT_ALLOWED, "O método %s não é suportado por este recurso."),
    REQUEST_FAILED(HttpStatus.BAD_REQUEST, "Não foi possível processar a requisição.");

    private final HttpStatus status;
    private final String message;
}
