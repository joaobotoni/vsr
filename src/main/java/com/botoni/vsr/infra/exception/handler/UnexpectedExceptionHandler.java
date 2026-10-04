package com.botoni.vsr.infra.exception.handler;

import com.botoni.vsr.infra.exception.problems.UnexpectedProblem;
import com.botoni.vsr.infra.exception.lib.problem.Problems;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.LOWEST_PRECEDENCE)
@RestControllerAdvice
public class UnexpectedExceptionHandler {

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception exception) {
        if (exception instanceof ErrorResponse) {
            return Problems.of(UnexpectedProblem.REQUEST_FAILED).status(exception).build();
        }
        return Problems.of(UnexpectedProblem.UNEXPECTED_ERROR).build();
    }
}
