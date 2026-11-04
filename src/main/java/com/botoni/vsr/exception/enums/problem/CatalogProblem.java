package com.botoni.vsr.exception.enums.problem;

import com.botoni.vsr.exception.lib.problem.Problem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum CatalogProblem implements Problem {

    ROOM_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "O tipo de ambiente informado não foi encontrado."),
    ITEM_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "O tipo de item informado não foi encontrado.");

    private final HttpStatus status;
    private final String message;
}
