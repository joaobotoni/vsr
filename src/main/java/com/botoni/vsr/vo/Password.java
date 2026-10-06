package com.botoni.vsr.vo;

import com.botoni.vsr.exception.custom.PasswordException;
import com.botoni.vsr.exception.enums.problem.PasswordProblem;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public record Password(@JsonValue String value) {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_BYTES = 72;

    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[^A-Za-z0-9\\s]");
    private static final Pattern WHITESPACE = Pattern.compile("\\s");

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
            throw new PasswordException(PasswordProblem.TOO_LONG);
        }
        if (lacksUppercase(value)) {
            throw new PasswordException(PasswordProblem.MISSING_UPPERCASE);
        }
        if (lacksLowercase(value)) {
            throw new PasswordException(PasswordProblem.MISSING_LOWERCASE);
        }
        if (lacksDigit(value)) {
            throw new PasswordException(PasswordProblem.MISSING_DIGIT);
        }
        if (lacksSpecialCharacter(value)) {
            throw new PasswordException(PasswordProblem.MISSING_SPECIAL_CHARACTER);
        }
        if (containsWhitespace(value)) {
            throw new PasswordException(PasswordProblem.CONTAINS_WHITESPACE);
        }
    }

    private static boolean isShort(String value) {
        return value.length() < MIN_LENGTH;
    }

    private static boolean isLong(String value) {
        return value.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES;
    }

    private static boolean lacksUppercase(String value) {
        return lacks(UPPERCASE, value);
    }

    private static boolean lacksLowercase(String value) {
        return lacks(LOWERCASE, value);
    }

    private static boolean lacksDigit(String value) {
        return lacks(DIGIT, value);
    }

    private static boolean lacksSpecialCharacter(String value) {
        return lacks(SPECIAL, value);
    }

    private static boolean containsWhitespace(String value) {
        return contains(WHITESPACE, value);
    }

    private static boolean lacks(Pattern pattern, String value) {
        return !contains(pattern, value);
    }

    private static boolean contains(Pattern pattern, String value) {
        return pattern.matcher(value).find();
    }

    @Override
    public String toString() {
        return "****";
    }
}
