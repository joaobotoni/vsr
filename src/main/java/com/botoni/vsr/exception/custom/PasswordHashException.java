package com.botoni.vsr.exception.custom;

import com.botoni.vsr.exception.enums.problem.PasswordHashProblem;

public final class PasswordHashException extends IllegalArgumentException {

    private final PasswordHashProblem problem;

    public PasswordHashException(PasswordHashProblem problem, Object... args) {
        super(String.format(problem.message(), args));
        this.problem = problem;
    }

    public PasswordHashProblem problem() {
        return problem;
    }
}
