package com.botoni.vsr.exception.handler;

import com.botoni.vsr.exception.enums.constraint.CheckConstraint;
import com.botoni.vsr.exception.enums.constraint.ForeignKeyConstraint;
import com.botoni.vsr.exception.enums.constraint.RuleConstraint;
import com.botoni.vsr.exception.enums.constraint.UniqueConstraint;
import com.botoni.vsr.exception.enums.problem.ConstraintProblem;
import com.botoni.vsr.exception.enums.problem.RegisterProblem;
import com.botoni.vsr.exception.lib.constraint.Constraint;
import com.botoni.vsr.exception.lib.constraint.Constraints;
import com.botoni.vsr.exception.lib.problem.Problem;
import com.botoni.vsr.exception.lib.problem.Problems;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.EnumSet;
import java.util.Set;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ConstraintExceptionHandler {

    private static final Constraints CONSTRAINTS = Constraints.of(
            CheckConstraint.values(),
            ForeignKeyConstraint.values(),
            UniqueConstraint.values(),
            RuleConstraint.values()
    );

    private static final Set<UniqueConstraint> IDENTITY = EnumSet.of(
            UniqueConstraint.UQ_PESSOA_FISICA_CPF,
            UniqueConstraint.UQ_USUARIO_EMAIL
    );

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException exception) {
        return Problems.of(masked(CONSTRAINTS.find(exception)), ConstraintProblem.DATA_INTEGRITY_VIOLATION).build();
    }

    private static Problem masked(Constraint constraint) {
        if (IDENTITY.contains(constraint)) {
            return RegisterProblem.UNAVAILABLE;
        }
        return constraint;
    }
}
