package com.botoni.vsr.exception.custom;

import com.botoni.vsr.exception.enums.problem.EmailProblem;

public final class EmailException extends IllegalArgumentException {

    private final EmailProblem problem;

    public EmailException(EmailProblem problem, Object... args) {
        super(String.format(problem.message(), args));
        this.problem = problem;
    }

    public EmailProblem problem() {
        return problem;
    }
}
