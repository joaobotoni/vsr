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

    MISSING_DETAILS(HttpStatus.INTERNAL_SERVER_ERROR, "Os dados do e-mail são obrigatórios."),
    MISSING_ATTACHMENTS(HttpStatus.INTERNAL_SERVER_ERROR, "Informe ao menos um anexo.");

    private final HttpStatus status;
    private final String message;
}
