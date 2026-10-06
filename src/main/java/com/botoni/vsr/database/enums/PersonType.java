package com.botoni.vsr.database.enums;

import com.botoni.vsr.exception.custom.PersonTypeException;
import com.botoni.vsr.exception.enums.problem.PersonTypeProblem;

import lombok.Getter;

@Getter
public enum PersonType {

    INDIVIDUAL("pf"),
    COMPANY("pj");

    private final String value;

    PersonType(String value) {
        this.value = value;
    }

    public static PersonType fromValue(String value) {
        for (PersonType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new PersonTypeException(PersonTypeProblem.UNKNOWN, value);
    }
}
