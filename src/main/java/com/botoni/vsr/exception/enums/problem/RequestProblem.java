package com.botoni.vsr.exception.enums.problem;

import com.botoni.vsr.exception.lib.problem.Problem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum RequestProblem implements Problem {

    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "O recurso solicitado não foi encontrado."),
    METHOD_NOT_SUPPORTED(HttpStatus.METHOD_NOT_ALLOWED, "O método %s não é suportado por este recurso."),
    REQUEST_FAILED(HttpStatus.BAD_REQUEST, "Não foi possível processar a requisição."),
    BODY_TOO_LARGE(HttpStatus.CONTENT_TOO_LARGE, "O corpo da requisição deve ter no máximo %d bytes."),
    LENGTH_REQUIRED(HttpStatus.LENGTH_REQUIRED, "O cabeçalho Content-Length é obrigatório."),
    INVALID_ADDRESS(HttpStatus.BAD_REQUEST, "O endereço de origem da requisição é inválido.");

    private final HttpStatus status;
    private final String message;
}
