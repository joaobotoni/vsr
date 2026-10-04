package com.botoni.vsr.infra.exception.handler;

import com.botoni.vsr.credential.exception.CredentialException;
import com.botoni.vsr.infra.exception.lib.problem.Problems;
import com.botoni.vsr.user.exception.UserNotFoundException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ApplicationExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    ProblemDetail handleNotFound(UserNotFoundException exception) {
        return Problems.of(exception, HttpStatus.NOT_FOUND).build();
    }

    @ExceptionHandler(CredentialException.class)
    ProblemDetail handleCredential(CredentialException exception) {
        return Problems.of(exception, HttpStatus.UNPROCESSABLE_CONTENT).build();
    }
}
