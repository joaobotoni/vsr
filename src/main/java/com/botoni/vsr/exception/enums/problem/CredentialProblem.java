package com.botoni.vsr.exception.enums.problem;

import com.botoni.vsr.exception.lib.problem.Problem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum CredentialProblem implements Problem {

    NOT_FOUND(HttpStatus.NOT_FOUND, "A credencial informada não foi encontrada."),
    INCORRECT_CURRENT_PASSWORD(HttpStatus.UNPROCESSABLE_CONTENT, "A senha atual informada está incorreta."),
    SAME_PASSWORD(HttpStatus.UNPROCESSABLE_CONTENT, "A nova senha deve ser diferente da senha atual.");

    private final HttpStatus status;
    private final String message;
}
