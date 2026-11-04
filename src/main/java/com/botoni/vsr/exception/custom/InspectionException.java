package com.botoni.vsr.exception.custom;

import com.botoni.vsr.exception.enums.problem.InspectionProblem;

public final class InspectionException extends RuntimeException {

    private final InspectionProblem problem;

    public InspectionException(InspectionProblem problem, Object... args) {
        super(String.format(problem.message(), args));
        this.problem = problem;
    }

    public InspectionProblem problem() {
        return problem;
    }
}
