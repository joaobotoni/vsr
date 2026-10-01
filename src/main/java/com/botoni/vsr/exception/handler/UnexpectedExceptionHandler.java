package com.botoni.vsr.exception.handler;

import com.botoni.vsr.exception.handler.problems.UnexpectedProblem;
import com.botoni.vsr.exception.lib.problem.Problems;

import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class UnexpectedExceptionHandler {

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception exception) {
        return Problems.of(exception, UnexpectedProblem.REQUEST_FAILED, UnexpectedProblem.UNEXPECTED_ERROR);
    }
}
