package com.botoni.vsr.exception.enums.problem;

import com.botoni.vsr.exception.lib.problem.Problem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum StorageProblem implements Problem {

    WRITE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Não foi possível gravar o arquivo da evidência."),
    READ_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Não foi possível ler o arquivo da evidência."),
    INTEGRITY(HttpStatus.INTERNAL_SERVER_ERROR, "O arquivo da evidência não confere com o registrado no envio."),
    OUTSIDE_DIRECTORY(HttpStatus.INTERNAL_SERVER_ERROR, "O arquivo da evidência está fora do diretório configurado.");

    private final HttpStatus status;
    private final String message;
}
