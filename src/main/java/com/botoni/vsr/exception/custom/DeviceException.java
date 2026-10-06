package com.botoni.vsr.exception.custom;

import com.botoni.vsr.exception.enums.problem.DeviceProblem;

public final class DeviceException extends RuntimeException {

    private final DeviceProblem problem;

    public DeviceException(DeviceProblem problem, Object... args) {
        super(String.format(problem.message(), args));
        this.problem = problem;
    }

    public DeviceProblem problem() {
        return problem;
    }
}
