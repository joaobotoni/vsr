package com.botoni.vsr.exception.custom;

import com.botoni.vsr.exception.enums.problem.SessionProblem;

public final class SessionException extends RuntimeException {

    private final SessionProblem problem;

    public SessionException(SessionProblem problem, Object... args) {
        super(String.format(problem.message(), args));
        this.problem = problem;
    }

    public SessionProblem problem() {
        return problem;
    }
}
