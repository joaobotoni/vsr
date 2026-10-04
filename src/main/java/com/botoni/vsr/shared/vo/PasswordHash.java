package com.botoni.vsr.shared.vo;

import com.botoni.vsr.shared.vo.exception.PasswordHashException;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.regex.Pattern;

public record PasswordHash(@JsonValue String value) {

    private static final int MIN_LENGTH = 20;
    private static final int MAX_LENGTH = 255;

    private static final Pattern WHITESPACE = Pattern.compile("\\s");
    private static final Pattern PRINTABLE_ASCII = Pattern.compile("^[\\x21-\\x7E]+$");

    public PasswordHash {
        if (value == null) {
            throw new PasswordHashException.Missing();
        }
        validate(value);
    }

    public static PasswordHash of(String value) {
        return new PasswordHash(value);
    }

    private static void validate(String value) {
        if (value.isBlank()) {
            throw new PasswordHashException.Missing();
        }
        if (isShort(value)) {
            throw new PasswordHashException.TooShort(MIN_LENGTH);
        }
        if (isLong(value)) {
            throw new PasswordHashException.TooLong(MAX_LENGTH);
        }
        if (containsWhitespace(value)) {
            throw new PasswordHashException.ContainsWhitespace();
        }
        if (hasInvalidCharacters(value)) {
            throw new PasswordHashException.InvalidCharacters();
        }
    }

    private static boolean isShort(String value) {
        return value.length() < MIN_LENGTH;
    }

    private static boolean isLong(String value) {
        return value.length() > MAX_LENGTH;
    }

    private static boolean containsWhitespace(String value) {
        return WHITESPACE.matcher(value).find();
    }

    private static boolean hasInvalidCharacters(String value) {
        return !PRINTABLE_ASCII.matcher(value).matches();
    }

    @Override
    public String toString() {
        return "****";
    }
}