package com.botoni.vsr.database.enums;

import com.botoni.vsr.exception.custom.PropertyException;
import com.botoni.vsr.exception.enums.problem.PropertyProblem;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

@Getter
public enum PropertyType {

    APARTMENT("apartamento"),
    HOUSE("casa"),
    COMMERCIAL_ROOM("sala_comercial"),
    WAREHOUSE("galpao"),
    OTHER("outro");

    @JsonValue
    private final String value;

    PropertyType(String value) {
        this.value = value;
    }

    public static PropertyType from(String value) {
        for (PropertyType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new PropertyException(PropertyProblem.UNKNOWN_TYPE, value);
    }
}
