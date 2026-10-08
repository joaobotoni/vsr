package com.botoni.vsr.exception.enums.problem;

import com.botoni.vsr.exception.lib.problem.Problem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum JwtProblem implements Problem {

    MISSING_USER(HttpStatus.INTERNAL_SERVER_ERROR, "O usuário é obrigatório para emitir o token."),
    MISSING_SESSION(HttpStatus.INTERNAL_SERVER_ERROR, "A sessão é obrigatória para emitir o token."),
    MISSING_SECRET(HttpStatus.INTERNAL_SERVER_ERROR, "A chave JWT não foi configurada."),
    INVALID_SECRET(HttpStatus.INTERNAL_SERVER_ERROR, "A chave JWT deve estar codificada em Base64."),
    WEAK_SECRET(HttpStatus.INTERNAL_SERVER_ERROR, "A chave JWT precisa ter pelo menos 256 bits (32 bytes)."),
    MISSING_ISSUER(HttpStatus.INTERNAL_SERVER_ERROR, "O issuer do JWT não foi configurado."),
    MISSING_TOKEN(HttpStatus.UNAUTHORIZED, "O token de acesso não foi informado."),
    INVALID_SUBJECT(HttpStatus.UNAUTHORIZED, "O token de acesso é inválido.");

    private final HttpStatus status;
    private final String message;
}
