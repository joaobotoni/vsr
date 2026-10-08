package com.botoni.vsr.exception.enums.problem;

import com.botoni.vsr.exception.lib.problem.Problem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum RegisterProblem implements Problem {

    UNAVAILABLE(HttpStatus.CONFLICT, "Não foi possível concluir o cadastro com os dados informados.");

    private final HttpStatus status;
    private final String message;
}
