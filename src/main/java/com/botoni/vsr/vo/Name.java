package com.botoni.vsr.vo;

import com.botoni.vsr.exception.custom.NameException;
import com.botoni.vsr.exception.enums.problem.NameProblem;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public record Name(@JsonValue String value) {

    private static final int MAX_LENGTH = 200;

    public Name {
        if (value == null) {
            throw new NameException(NameProblem.MISSING);
        }
        value = value.trim();
        validate(value);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Name of(String value) {
        return new Name(value);
    }

    private static void validate(String value) {
        if (value.isEmpty()) {
            throw new NameException(NameProblem.MISSING);
        }
        if (isLong(value)) {
            throw new NameException(NameProblem.TOO_LONG, MAX_LENGTH);
        }
    }

    private static boolean isLong(String value) {
        return value.codePointCount(0, value.length()) > MAX_LENGTH;
    }
}
