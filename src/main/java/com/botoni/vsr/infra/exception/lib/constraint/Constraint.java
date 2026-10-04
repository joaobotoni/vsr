package com.botoni.vsr.infra.exception.lib.constraint;

import com.botoni.vsr.infra.exception.lib.problem.Problem;

public interface Constraint extends Problem {
    String constraint();
}
