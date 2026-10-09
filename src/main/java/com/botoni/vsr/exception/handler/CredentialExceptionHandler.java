package com.botoni.vsr.exception.handler;

import com.botoni.vsr.exception.custom.CredentialException;
import com.botoni.vsr.exception.lib.problem.Problems;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class CredentialExceptionHandler {

    @ExceptionHandler(CredentialException.class)
    ProblemDetail handle(CredentialException exception) {
        return Problems.of(exception, exception.problem()).build();
    }
}
