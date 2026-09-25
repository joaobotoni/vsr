package com.botoni.vsr.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum PersonType {

    INDIVIDUAL("pf"),
    COMPANY("pj");
    private static final String UNKNOWN_PERSON_TYPE = "Unknown person type:\t";

    private final String value;

    PersonType(String value) {
        this.value = value;
    }

    public static PersonType fromValue(String value) {
        return Arrays.stream(values())
                .filter(type -> type.hasValue(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(UNKNOWN_PERSON_TYPE + value));
    }

    private boolean hasValue(String candidate) {
        return value.equals(candidate);
    }
}
