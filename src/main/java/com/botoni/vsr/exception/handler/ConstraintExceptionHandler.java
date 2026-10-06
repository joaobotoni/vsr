package com.botoni.vsr.exception.handler;

import com.botoni.vsr.exception.enums.constraint.CheckConstraint;
import com.botoni.vsr.exception.enums.constraint.ForeignKeyConstraint;
import com.botoni.vsr.exception.enums.constraint.UniqueConstraint;
import com.botoni.vsr.exception.enums.problem.ConstraintProblem;
import com.botoni.vsr.exception.lib.constraint.Constraints;
import com.botoni.vsr.exception.lib.problem.Problems;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ConstraintExceptionHandler {

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
