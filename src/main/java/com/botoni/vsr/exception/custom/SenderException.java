package com.botoni.vsr.exception.custom;

import com.botoni.vsr.exception.enums.problem.SenderProblem;

public final class SenderException extends IllegalArgumentException {

    private final SenderProblem problem;

    public SenderException(SenderProblem problem, Object... args) {
        super(String.format(problem.message(), args));
        this.problem = problem;
    }

    public SenderException(SenderProblem problem, Throwable cause) {
        super(problem.message(), cause);
        this.problem = problem;
    }

    public SenderProblem problem() {
        return problem;
    }
}
