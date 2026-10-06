package com.botoni.vsr.exception.handler;

import com.botoni.vsr.exception.custom.UserException;
import com.botoni.vsr.exception.lib.problem.Problems;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class UserExceptionHandler {

    @ExceptionHandler(UserException.class)
    ProblemDetail handleUser(UserException exception) {
        return Problems.of(exception, exception.problem()).build();
    }
}
