package com.botoni.vsr.exception.enums.problem;

import com.botoni.vsr.exception.lib.problem.Problem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum OpaqueTokenProblem implements Problem {

    MISSING_ALGORITHM(HttpStatus.INTERNAL_SERVER_ERROR, "O algoritmo de hash do token não está disponível."),
    MISSING_SECRET(HttpStatus.INTERNAL_SERVER_ERROR, "A chave do refresh token não foi configurada."),
    INVALID_SECRET(HttpStatus.INTERNAL_SERVER_ERROR, "A chave do refresh token deve estar codificada em Base64."),
    WEAK_SECRET(HttpStatus.INTERNAL_SERVER_ERROR, "A chave do refresh token precisa ter pelo menos 256 bits (32 bytes).");

    private final HttpStatus status;
    private final String message;
}
