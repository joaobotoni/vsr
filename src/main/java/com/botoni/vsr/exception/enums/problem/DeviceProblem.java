package com.botoni.vsr.exception.enums.problem;

import com.botoni.vsr.exception.lib.problem.Problem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum DeviceProblem implements Problem {

    NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "O dispositivo não foi encontrado após o registro.");

    private final HttpStatus status;
    private final String message;
}
