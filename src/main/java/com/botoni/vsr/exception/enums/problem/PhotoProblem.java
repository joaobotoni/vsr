package com.botoni.vsr.exception.enums.problem;

import com.botoni.vsr.exception.lib.problem.Problem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum PhotoProblem implements Problem {

    EMPTY(HttpStatus.BAD_REQUEST, "O arquivo enviado está vazio."),
    UNSUPPORTED(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Formato de arquivo não suportado. São aceitas apenas imagens JPEG ou PNG."),
    UNREADABLE(HttpStatus.BAD_REQUEST, "Não foi possível ler o arquivo enviado.");

    private final HttpStatus status;
    private final String message;
}
