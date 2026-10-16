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
    EXPIRED(HttpStatus.UNAUTHORIZED, "A sessão expirou. Faça login novamente."),
    IDLE(HttpStatus.UNAUTHORIZED, "A sessão expirou por inatividade. Faça login novamente."),
    INVALID_TTL(HttpStatus.INTERNAL_SERVER_ERROR, "A validade da sessão deve ser maior que zero."),
    INVALID_IDLE(HttpStatus.INTERNAL_SERVER_ERROR, "O tempo máximo de inatividade da sessão deve ser maior que zero."),
    IDLE_TOO_LONG(HttpStatus.INTERNAL_SERVER_ERROR, "O tempo máximo de inatividade (%s) deve ser menor que a validade da sessão (%s).");

    private final HttpStatus status;
    private final String message;
}
