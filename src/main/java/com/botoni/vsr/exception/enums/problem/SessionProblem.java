package com.botoni.vsr.exception.enums.problem;

import com.botoni.vsr.exception.lib.problem.Problem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum SessionProblem implements Problem {

    NOT_FOUND(HttpStatus.UNAUTHORIZED, "A sessão informada não foi encontrada."),
    REVOKED(HttpStatus.UNAUTHORIZED, "A sessão foi encerrada. Faça login novamente."),
    EXPIRED(HttpStatus.UNAUTHORIZED, "A sessão expirou. Faça login novamente.");

    private final HttpStatus status;
    private final String message;
}
