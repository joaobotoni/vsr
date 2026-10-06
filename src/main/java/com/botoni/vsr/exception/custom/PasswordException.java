package com.botoni.vsr.exception.custom;

import com.botoni.vsr.exception.enums.problem.PasswordProblem;

public final class PasswordException extends IllegalArgumentException {

    private final PasswordProblem problem;

    public PasswordException(PasswordProblem problem, Object... args) {
        super(String.format(problem.message(), args));
        this.problem = problem;
    }

    public PasswordProblem problem() {
        return problem;
    }
}
