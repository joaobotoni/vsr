package com.botoni.vsr.exception.custom;

import com.botoni.vsr.exception.enums.problem.CnpjProblem;

public final class CnpjException extends IllegalArgumentException {

    private final CnpjProblem problem;

    public CnpjException(CnpjProblem problem, Object... args) {
        super(String.format(problem.message(), args));
        this.problem = problem;
    }

    public CnpjProblem problem() {
        return problem;
    }
}
