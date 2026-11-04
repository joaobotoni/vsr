package com.botoni.vsr.database.enums;

import com.botoni.vsr.exception.custom.InspectionException;
import com.botoni.vsr.exception.enums.problem.InspectionProblem;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

@Getter
public enum InspectionType {

    ENTRY("entrada"),
    EXIT("saida");

    @JsonValue
    private final String value;

    InspectionType(String value) {
        this.value = value;
    }

    public static InspectionType from(String value) {
        for (InspectionType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new InspectionException(InspectionProblem.UNKNOWN_TYPE, value);
    }
}
