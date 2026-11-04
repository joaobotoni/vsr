package com.botoni.vsr.exception.enums.problem;

import com.botoni.vsr.exception.lib.problem.Problem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum PropertyProblem implements Problem {

    NOT_FOUND(HttpStatus.NOT_FOUND, "O imóvel solicitado não foi encontrado."),
    UNKNOWN_TYPE(HttpStatus.INTERNAL_SERVER_ERROR, "O tipo de imóvel %s é desconhecido."),
    UNKNOWN_CATEGORY(HttpStatus.INTERNAL_SERVER_ERROR, "A categoria de imóvel %s é desconhecida.");

    private final HttpStatus status;
    private final String message;
}
