package com.botoni.vsr.infra.exception.handler;

import com.botoni.vsr.infra.exception.constraints.CheckConstraint;
import com.botoni.vsr.infra.exception.constraints.ForeignKeyConstraint;
import com.botoni.vsr.infra.exception.constraints.UniqueConstraint;
import com.botoni.vsr.infra.exception.lib.problem.Problems;
import com.botoni.vsr.infra.exception.lib.constraint.Constraints;
import com.botoni.vsr.infra.exception.problems.ConstraintProblem;

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
