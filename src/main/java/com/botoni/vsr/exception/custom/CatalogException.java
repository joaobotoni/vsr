package com.botoni.vsr.exception.custom;

import com.botoni.vsr.exception.enums.problem.CatalogProblem;

public final class CatalogException extends RuntimeException {

    private final CatalogProblem problem;

    public CatalogException(CatalogProblem problem, Object... args) {
        super(String.format(problem.message(), args));
        this.problem = problem;
    }

    public CatalogProblem problem() {
        return problem;
    }
}
