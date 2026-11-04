package com.botoni.vsr.exception.custom;

import com.botoni.vsr.exception.enums.problem.StorageProblem;

public final class StorageException extends RuntimeException {

    private final StorageProblem problem;

    public StorageException(StorageProblem problem, Object... args) {
        super(String.format(problem.message(), args));
        this.problem = problem;
    }

    public StorageException(StorageProblem problem, Throwable cause) {
        super(problem.message(), cause);
        this.problem = problem;
    }

    public StorageProblem problem() {
        return problem;
    }
}
