package com.botoni.vsr.exception.custom;

import com.botoni.vsr.exception.enums.problem.PersonTypeProblem;

public final class PersonTypeException extends RuntimeException {

    private final PersonTypeProblem problem;

    public PersonTypeException(PersonTypeProblem problem, Object... args) {
        super(String.format(problem.message(), args));
        this.problem = problem;
    }

    public PersonTypeProblem problem() {
        return problem;
    }
}
