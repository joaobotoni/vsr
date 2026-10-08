package com.botoni.vsr.exception.enums.problem;

import com.botoni.vsr.exception.lib.problem.Problem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum RefreshTokenProblem implements Problem {

    NOT_FOUND(HttpStatus.UNAUTHORIZED, "O refresh token informado não foi encontrado."),
    REUSED(HttpStatus.UNAUTHORIZED, "O refresh token já foi utilizado. A sessão foi encerrada.");

    private final HttpStatus status;
    private final String message;
}
