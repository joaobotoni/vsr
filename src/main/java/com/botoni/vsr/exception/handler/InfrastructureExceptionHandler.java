package com.botoni.vsr.exception.handler;

import com.botoni.vsr.exception.lib.problem.Problems;
import com.botoni.vsr.exception.infrastructure.RateLimitException;
import com.botoni.vsr.exception.infrastructure.UnknownTypeException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class InfrastructureExceptionHandler {

    @ExceptionHandler(RateLimitException.Exceeded.class)
    ProblemDetail handleRateLimitExceeded(RateLimitException.Exceeded exception) {
        return Problems.of(exception, HttpStatus.TOO_MANY_REQUESTS).build();
    }

    @ExceptionHandler(UnknownTypeException.class)
    ProblemDetail handleUnknownType(UnknownTypeException exception) {
        return Problems.of(exception, HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
