package com.botoni.vsr.exception.custom;

import com.botoni.vsr.exception.enums.problem.OpaqueTokenProblem;

public final class OpaqueTokenException extends RuntimeException {

    private final OpaqueTokenProblem problem;

    public OpaqueTokenException(OpaqueTokenProblem problem, Object... args) {
        super(String.format(problem.message(), args));
        this.problem = problem;
    }

    public OpaqueTokenProblem problem() {
        return problem;
    }
}
