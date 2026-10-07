package com.botoni.vsr.exception.enums.problem;

import com.botoni.vsr.exception.lib.problem.Problem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum DetailsProblem implements Problem {

    MISSING_SENDER(HttpStatus.INTERNAL_SERVER_ERROR, "O remetente do e-mail é obrigatório."),
    MISSING_RECIPIENT(HttpStatus.INTERNAL_SERVER_ERROR, "O destinatário do e-mail é obrigatório."),
    MISSING_SUBJECT(HttpStatus.INTERNAL_SERVER_ERROR, "O assunto do e-mail é obrigatório."),
    SUBJECT_TOO_LONG(HttpStatus.INTERNAL_SERVER_ERROR, "O assunto do e-mail deve conter no máximo %d caracteres."),
    INVALID_SUBJECT(HttpStatus.INTERNAL_SERVER_ERROR, "O assunto do e-mail não pode conter quebras de linha."),
    MISSING_BODY(HttpStatus.INTERNAL_SERVER_ERROR, "O corpo do e-mail é obrigatório.");

    private final HttpStatus status;
    private final String message;
}
