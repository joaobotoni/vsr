package com.botoni.vsr.exception.enums.problem;

import com.botoni.vsr.exception.lib.problem.Problem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum PersonProblem implements Problem {

    NOT_FOUND(HttpStatus.NOT_FOUND, "A pessoa solicitada não foi encontrada.");

    private final HttpStatus status;
    private final String message;
}
