package com.botoni.vsr.exception.enums.problem;

import com.botoni.vsr.exception.lib.problem.Problem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum EmailProblem implements Problem {

    MISSING(HttpStatus.BAD_REQUEST, "O e-mail é obrigatório."),
    TOO_LONG(HttpStatus.BAD_REQUEST, "O e-mail deve conter no máximo %d caracteres."),
    CONTAINS_WHITESPACE(HttpStatus.BAD_REQUEST, "O e-mail não pode conter espaços em branco."),
    MISSING_AT_SIGN(HttpStatus.BAD_REQUEST, "O e-mail deve conter o caractere '@'."),
    MULTIPLE_AT_SIGNS(HttpStatus.BAD_REQUEST, "O e-mail deve conter apenas um caractere '@'."),
    LOCAL_PART_TOO_LONG(HttpStatus.BAD_REQUEST, "O nome de usuário do e-mail, antes do '@', deve conter no máximo %d caracteres."),
    INVALID_LOCAL_PART(HttpStatus.BAD_REQUEST, "O nome de usuário do e-mail, antes do '@', é inválido."),
    INVALID_DOMAIN(HttpStatus.BAD_REQUEST, "O domínio do e-mail é inválido."),
    INVALID_TOP_LEVEL_DOMAIN(HttpStatus.BAD_REQUEST, "A extensão do domínio do e-mail é inválida.");

    private final HttpStatus status;
    private final String message;
}
