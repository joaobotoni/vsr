package com.botoni.vsr.database.enums;

import com.botoni.vsr.exception.custom.PropertyException;
import com.botoni.vsr.exception.enums.problem.PropertyProblem;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

@Getter
public enum PropertyCategory {

    RESIDENTIAL("residencial"),
    COMMERCIAL("comercial"),
    HIGH_END("alto_padrao");

    @JsonValue
    private final String value;

    PropertyCategory(String value) {
        this.value = value;
    }

    public static PropertyCategory from(String value) {
        for (PropertyCategory category : values()) {
            if (category.value.equals(value)) {
                return category;
            }
        }
        throw new PropertyException(PropertyProblem.UNKNOWN_CATEGORY, value);
    }
}
