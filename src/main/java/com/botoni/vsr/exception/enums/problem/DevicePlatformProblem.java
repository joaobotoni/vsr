package com.botoni.vsr.exception.enums.problem;

import com.botoni.vsr.exception.lib.problem.Problem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum DevicePlatformProblem implements Problem {

    UNKNOWN(HttpStatus.INTERNAL_SERVER_ERROR, "A plataforma de dispositivo %s é desconhecida.");

    private final HttpStatus status;
    private final String message;
}
