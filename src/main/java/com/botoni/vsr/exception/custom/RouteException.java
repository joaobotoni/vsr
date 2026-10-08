package com.botoni.vsr.exception.custom;

import com.botoni.vsr.exception.enums.problem.RouteProblem;

public final class RouteException extends IllegalArgumentException {

    private final RouteProblem problem;

    public RouteException(RouteProblem problem, Object... args) {
        super(String.format(problem.message(), args));
        this.problem = problem;
    }

    public RouteProblem problem() {
        return problem;
    }
}
