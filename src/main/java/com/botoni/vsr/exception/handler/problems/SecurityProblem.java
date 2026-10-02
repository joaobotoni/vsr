package com.botoni.vsr.exception.handler.problems;

import com.botoni.vsr.exception.lib.problem.Problem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum SecurityProblem implements Problem {

    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "O token de acesso é inválido ou expirou."),
    BAD_CREDENTIALS(HttpStatus.UNAUTHORIZED, "E-mail ou senha incorretos."),
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "Não foi possível autenticar a requisição."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "O acesso ao recurso solicitado foi negado.");

    private final HttpStatus status;
    private final String message;
}
