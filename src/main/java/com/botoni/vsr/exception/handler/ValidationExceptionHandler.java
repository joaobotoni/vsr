package com.botoni.vsr.exception.handler;

import com.botoni.vsr.exception.handler.problems.ValidationProblem;
import com.botoni.vsr.exception.lib.problem.Problems;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ValidationExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        return Problems.of(ValidationProblem.INVALID_DATA).errors(exception.getFieldErrors()).build();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail handleHttpMessageNotReadable() {
        return Problems.of(ValidationProblem.UNREADABLE_BODY).build();
    }
}
