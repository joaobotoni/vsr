package com.botoni.vsr.exception.enums.problem;

import com.botoni.vsr.exception.lib.problem.Problem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum InspectionProblem implements Problem {

    NOT_FOUND(HttpStatus.NOT_FOUND, "A vistoria solicitada não foi encontrada."),
    ENTRY_NOT_FOUND(HttpStatus.NOT_FOUND, "A vistoria de entrada informada não foi encontrada."),
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "O ambiente solicitado não foi encontrado."),
    ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "O item solicitado não foi encontrado."),
    EVIDENCE_NOT_FOUND(HttpStatus.NOT_FOUND, "A evidência solicitada não foi encontrada."),
    UNKNOWN_TYPE(HttpStatus.INTERNAL_SERVER_ERROR, "O tipo de vistoria %s é desconhecido."),
    UNKNOWN_STATUS(HttpStatus.INTERNAL_SERVER_ERROR, "O status de vistoria %s é desconhecido."),
    UNKNOWN_CONDITION(HttpStatus.INTERNAL_SERVER_ERROR, "O estado de item %s é desconhecido.");

    private final HttpStatus status;
    private final String message;
}
