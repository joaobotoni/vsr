package com.botoni.vsr.exception.enums.problem;

import com.botoni.vsr.exception.lib.problem.Problem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum NameProblem implements Problem {

    MISSING(HttpStatus.BAD_REQUEST, "O nome é obrigatório."),
    TOO_LONG(HttpStatus.BAD_REQUEST, "O nome deve conter no máximo %d caracteres.");

    private final HttpStatus status;
    private final String message;
}
