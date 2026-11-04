package com.botoni.vsr.exception.custom;

import com.botoni.vsr.exception.enums.problem.PhotoProblem;

public final class PhotoException extends RuntimeException {

    private final PhotoProblem problem;

    public PhotoException(PhotoProblem problem, Object... args) {
        super(String.format(problem.message(), args));
        this.problem = problem;
    }

    public PhotoException(PhotoProblem problem, Throwable cause) {
        super(problem.message(), cause);
        this.problem = problem;
    }

    public PhotoProblem problem() {
        return problem;
    }
}
