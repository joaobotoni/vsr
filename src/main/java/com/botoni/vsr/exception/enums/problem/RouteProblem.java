package com.botoni.vsr.exception.enums.problem;

import com.botoni.vsr.exception.lib.problem.Problem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum RouteProblem implements Problem {

    MISSING_MATCHER(HttpStatus.INTERNAL_SERVER_ERROR, "A rota precisa de um matcher."),
    MISSING_METHOD(HttpStatus.INTERNAL_SERVER_ERROR, "A rota precisa de um método HTTP."),
    INVALID_PATH(HttpStatus.INTERNAL_SERVER_ERROR, "O caminho da rota deve começar com '/': %s");

    private final HttpStatus status;
    private final String message;
}
