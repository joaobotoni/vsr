package com.botoni.vsr.exception.handler;

import com.botoni.vsr.exception.handler.constraints.CheckConstraint;
import com.botoni.vsr.exception.handler.constraints.ForeignKeyConstraint;
import com.botoni.vsr.exception.handler.constraints.UniqueConstraint;
import com.botoni.vsr.exception.lib.problem.Problems;
import com.botoni.vsr.exception.lib.constraint.Constraints;
import com.botoni.vsr.exception.handler.problems.ConstraintProblem;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class DatabaseExceptionHandler {

    private static final Constraints CONSTRAINTS = Constraints.of(
            CheckConstraint.values(),
            ForeignKeyConstraint.values(),
            UniqueConstraint.values()
    );

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException exception) {
        return Problems.of(CONSTRAINTS.find(exception), ConstraintProblem.DATA_INTEGRITY_VIOLATION).build();
    }
}
