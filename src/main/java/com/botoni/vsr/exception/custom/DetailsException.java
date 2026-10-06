package com.botoni.vsr.exception.custom;

import com.botoni.vsr.exception.enums.problem.DetailsProblem;

public final class DetailsException extends IllegalArgumentException {

    private final DetailsProblem problem;

    public DetailsException(DetailsProblem problem, Object... args) {
        super(String.format(problem.message(), args));
        this.problem = problem;
    }

    public DetailsProblem problem() {
        return problem;
    }
}
