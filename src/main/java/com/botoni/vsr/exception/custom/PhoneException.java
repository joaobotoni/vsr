package com.botoni.vsr.exception.custom;

import com.botoni.vsr.exception.enums.problem.PhoneProblem;

public final class PhoneException extends RuntimeException {

    private final PhoneProblem problem;

    public PhoneException(PhoneProblem problem, Object... args) {
        super(String.format(problem.message(), args));
        this.problem = problem;
    }

    public PhoneProblem problem() {
        return problem;
    }
}
