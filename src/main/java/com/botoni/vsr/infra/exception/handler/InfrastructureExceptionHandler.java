package com.botoni.vsr.infra.exception.handler;

import com.botoni.vsr.infra.exception.lib.problem.Problems;
import com.botoni.vsr.infra.ratelimit.exception.RateLimitException;
import com.botoni.vsr.person.exception.UnknownPersonTypeException;
import com.botoni.vsr.session.exception.UnknownDevicePlatformException;

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

    @ExceptionHandler({UnknownPersonTypeException.class, UnknownDevicePlatformException.class})
    ProblemDetail handleUnknownType(RuntimeException exception) {
        return Problems.of(exception, HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
