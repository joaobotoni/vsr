package com.botoni.vsr.exception.enums.problem;

import com.botoni.vsr.exception.lib.problem.Problem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum MatcherProblem implements Problem {

    MISSING_DELEGATE(HttpStatus.INTERNAL_SERVER_ERROR, "O matcher precisa de um RequestMatcher.");

    private final HttpStatus status;
    private final String message;
}
