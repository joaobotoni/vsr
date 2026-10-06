package com.botoni.vsr.exception.enums.problem;

import com.botoni.vsr.exception.lib.problem.Problem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum PersonTypeProblem implements Problem {

    UNKNOWN(HttpStatus.INTERNAL_SERVER_ERROR, "O tipo de pessoa %s é desconhecido.");

    private final HttpStatus status;
    private final String message;
}
