package com.botoni.vsr.exception.enums.problem;

import com.botoni.vsr.exception.lib.problem.Problem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum LimitProblem implements Problem {

    INVALID_CAPACITY(HttpStatus.INTERNAL_SERVER_ERROR, "A capacidade do rate limit deve ser maior que zero."),
    INVALID_REFILL_RATE(HttpStatus.INTERNAL_SERVER_ERROR, "A taxa de recarga do rate limit deve ser maior que zero."),
    INVALID_REFILL_INTERVAL(HttpStatus.INTERNAL_SERVER_ERROR, "O intervalo de recarga do rate limit deve ser maior que zero.");

    private final HttpStatus status;
    private final String message;
}
