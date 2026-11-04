package com.botoni.vsr.exception.enums.problem;

import com.botoni.vsr.exception.lib.problem.Problem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum CompanyProblem implements Problem {

    NOT_FOUND(HttpStatus.NOT_FOUND, "A empresa solicitada não foi encontrada."),
    ADMIN_REQUIRED(HttpStatus.FORBIDDEN, "Só sócios, representantes e MEI da empresa podem gerenciar os membros."),
    ADMIN_ROLE_REQUIRED(HttpStatus.BAD_REQUEST, "Quem cadastra a empresa precisa ser sócio, representante ou MEI."),
    UNKNOWN_ROLE(HttpStatus.INTERNAL_SERVER_ERROR, "O papel de membro %s é desconhecido.");

    private final HttpStatus status;
    private final String message;
}
