package com.botoni.vsr.exception.handler;

import com.botoni.vsr.exception.lib.problem.Problems;
import com.botoni.vsr.exception.infrastructure.CredentialException;
import com.botoni.vsr.exception.infrastructure.NotFoundException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApplicationExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    ProblemDetail handleNotFound(NotFoundException exception) {
        return Problems.of(exception, HttpStatus.NOT_FOUND).build();
    }

    @ExceptionHandler(CredentialException.class)
    ProblemDetail handleCredential(CredentialException exception) {
        return Problems.of(exception, HttpStatus.UNPROCESSABLE_CONTENT).build();
    }
}
