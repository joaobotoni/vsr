package com.botoni.vsr.exception.custom;

import com.botoni.vsr.exception.enums.problem.RefreshTokenProblem;

public final class RefreshTokenException extends RuntimeException {

    private final RefreshTokenProblem problem;

    public RefreshTokenException(RefreshTokenProblem problem, Object... args) {
        super(String.format(problem.message(), args));
        this.problem = problem;
    }

    public RefreshTokenProblem problem() {
        return problem;
    }
}
