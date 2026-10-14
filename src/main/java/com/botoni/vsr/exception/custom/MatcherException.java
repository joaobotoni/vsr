package com.botoni.vsr.exception.custom;

import com.botoni.vsr.exception.enums.problem.MatcherProblem;

public final class MatcherException extends IllegalArgumentException {

    private final MatcherProblem problem;

    public MatcherException(MatcherProblem problem, Object... args) {
        super(String.format(problem.message(), args));
        this.problem = problem;
    }

    public MatcherProblem problem() {
        return problem;
    }
}
