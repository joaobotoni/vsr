package com.botoni.vsr.vo;

import com.botoni.vsr.exception.custom.PasswordException;
import com.botoni.vsr.exception.enums.problem.PasswordProblem;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public record Password(@JsonValue String value) {

    private static final int MIN_LENGTH = 12;
    private static final int MAX_LENGTH = 128;

    public Password {
        if (value == null) {
            throw new PasswordException(PasswordProblem.MISSING);
        }
        validate(value);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Password of(String value) {
        return new Password(value);
    }

    private static void validate(String value) {
        if (value.isBlank()) {
            throw new PasswordException(PasswordProblem.MISSING);
        }
        if (isShort(value)) {
            throw new PasswordException(PasswordProblem.TOO_SHORT, MIN_LENGTH);
        }
        if (isLong(value)) {
            throw new PasswordException(PasswordProblem.TOO_LONG, MAX_LENGTH);
        }
    }

    private static boolean isShort(String value) {
        return length(value) < MIN_LENGTH;
    }

    private static boolean isLong(String value) {
        return length(value) > MAX_LENGTH;
    }

    private static int length(String value) {
        return value.codePointCount(0, value.length());
    }

    @Override
    public String toString() {
        return "****";
    }
}
