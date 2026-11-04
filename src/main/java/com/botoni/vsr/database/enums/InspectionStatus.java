package com.botoni.vsr.database.enums;

import com.botoni.vsr.exception.custom.InspectionException;
import com.botoni.vsr.exception.enums.problem.InspectionProblem;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

@Getter
public enum InspectionStatus {

    IN_PROGRESS("em_andamento"),
    FINISHED("finalizada");

    @JsonValue
    private final String value;

    InspectionStatus(String value) {
        this.value = value;
    }

    public static InspectionStatus from(String value) {
        for (InspectionStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new InspectionException(InspectionProblem.UNKNOWN_STATUS, value);
    }
}
