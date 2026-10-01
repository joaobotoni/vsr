package com.botoni.vsr.exception.handler;

import com.botoni.vsr.exception.lib.problem.Problems;
import com.botoni.vsr.exception.lib.constraint.Constraints;
import com.botoni.vsr.exception.handler.problems.ConstraintProblem;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class DatabaseExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException exception) {
        return Problems.of(Constraints.of(exception), ConstraintProblem.DATA_INTEGRITY_VIOLATION);
    }
}
