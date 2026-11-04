package com.botoni.vsr.exception.enums.problem;

import com.botoni.vsr.exception.lib.problem.Problem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum PhoneProblem implements Problem {

    MISSING(HttpStatus.BAD_REQUEST, "O telefone é obrigatório."),
    INVALID(HttpStatus.BAD_REQUEST, "O telefone deve ter DDD e número: celular com 9 dígitos começando por 9, ou fixo com 8 dígitos.");

    private final HttpStatus status;
    private final String message;
}
