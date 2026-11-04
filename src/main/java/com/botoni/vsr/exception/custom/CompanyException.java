package com.botoni.vsr.exception.custom;

import com.botoni.vsr.exception.enums.problem.CompanyProblem;

public final class CompanyException extends RuntimeException {

    private final CompanyProblem problem;

    public CompanyException(CompanyProblem problem, Object... args) {
        super(String.format(problem.message(), args));
        this.problem = problem;
    }

    public CompanyProblem problem() {
        return problem;
    }
}
