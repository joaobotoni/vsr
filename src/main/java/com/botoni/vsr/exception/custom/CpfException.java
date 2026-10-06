package com.botoni.vsr.exception.custom;

import com.botoni.vsr.exception.enums.problem.CpfProblem;

public final class CpfException extends IllegalArgumentException {

    private final CpfProblem problem;

    public CpfException(CpfProblem problem, Object... args) {
        super(String.format(problem.message(), args));
        this.problem = problem;
    }

    public CpfProblem problem() {
        return problem;
    }
}
