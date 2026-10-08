package com.botoni.vsr.exception.custom;

import com.botoni.vsr.exception.enums.problem.RequestProblem;

public final class RequestException extends RuntimeException {

    private final RequestProblem problem;

    public RequestException(RequestProblem problem, Object... args) {
        super(String.format(problem.message(), args));
        this.problem = problem;
    }

    public RequestProblem problem() {
        return problem;
    }
}