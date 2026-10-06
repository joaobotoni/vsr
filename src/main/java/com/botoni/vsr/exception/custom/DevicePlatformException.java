package com.botoni.vsr.exception.custom;

import com.botoni.vsr.exception.enums.problem.DevicePlatformProblem;

public final class DevicePlatformException extends RuntimeException {

    private final DevicePlatformProblem problem;

    public DevicePlatformException(DevicePlatformProblem problem, Object... args) {
        super(String.format(problem.message(), args));
        this.problem = problem;
    }

    public DevicePlatformProblem problem() {
        return problem;
    }
}
