package com.botoni.vsr.infra.exception.problems;

import com.botoni.vsr.infra.exception.lib.problem.Problem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum ConstraintProblem implements Problem {

    DATA_INTEGRITY_VIOLATION(HttpStatus.CONFLICT, "Os dados informados violam uma regra de integridade.");

    private final HttpStatus status;
    private final String message;
}
