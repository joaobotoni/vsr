package com.botoni.vsr.exception.enums.problem;

import com.botoni.vsr.exception.lib.problem.Problem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum SenderProblem implements Problem {

    MISSING_MAIL(HttpStatus.INTERNAL_SERVER_ERROR, "Os dados do e-mail são obrigatórios."),
    MISSING_ATTACHMENTS(HttpStatus.INTERNAL_SERVER_ERROR, "Informe ao menos um anexo."),
    ATTACHMENT_OUTSIDE_DIRECTORY(HttpStatus.INTERNAL_SERVER_ERROR, "O anexo deve estar dentro do diretório de anexos configurado."),
    ATTACHMENT_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "O anexo ou o diretório de anexos não existe."),
    DELIVERY_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "Não foi possível enviar o e-mail. Tente novamente mais tarde.");

    private final HttpStatus status;
    private final String message;
}
