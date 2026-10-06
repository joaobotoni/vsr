package com.botoni.vsr.exception.enums.problem;

import com.botoni.vsr.exception.lib.problem.Problem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum RateLimitProblem implements Problem {

    EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "O limite de requisições foi excedido. Tente novamente em %d segundos.");

    private final HttpStatus status;
    private final String message;
}
