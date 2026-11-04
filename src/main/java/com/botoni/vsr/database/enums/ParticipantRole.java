package com.botoni.vsr.database.enums;

import com.botoni.vsr.exception.custom.InspectionException;
import com.botoni.vsr.exception.enums.problem.InspectionProblem;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

@Getter
public enum ParticipantRole {

    OWNER("proprietario"),
    TENANT("inquilino");

    @JsonValue
    private final String value;

    ParticipantRole(String value) {
        this.value = value;
    }

    public static ParticipantRole from(String value) {
        for (ParticipantRole role : values()) {
            if (role.value.equals(value)) {
                return role;
            }
        }
        throw new InspectionException(InspectionProblem.UNKNOWN_ROLE, value);
    }
}
