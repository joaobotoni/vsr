package com.botoni.vsr.exception.handler;

import com.botoni.vsr.exception.custom.RateLimitException;
import com.botoni.vsr.exception.lib.problem.Problems;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class RateLimitExceptionHandler {

    @ExceptionHandler(RateLimitException.class)
    ProblemDetail handleRateLimit(RateLimitException exception) {
        return Problems.of(exception, exception.problem()).build();
    }
}
