package com.botoni.vsr.exception.enums.constraint;

import com.botoni.vsr.exception.lib.constraint.Constraint;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum RuleConstraint implements Constraint {

    RN_SESSAO_ATIVA("rn_sessao_ativa", HttpStatus.UNAUTHORIZED, "A sessão foi encerrada ou expirou. Faça login novamente."),
    RN_REFRESH_TOKEN_RENOVADO("rn_refresh_token_renovado", HttpStatus.CONFLICT, "O refresh token já foi renovado por outra requisição.");

    private final String constraint;
    private final HttpStatus status;
    private final String message;
}
