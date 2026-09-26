package com.botoni.vsr.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Locale;
import java.util.regex.Pattern;

public record Email(String value) {

    private static final String INVALID_EMAIL = "E-mail inválido";
    private static final Pattern FORMAT = Pattern.compile("^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$");
    private static final int MAX_LENGTH = 254;

    public Email {
        if (isInvalid(value)) {
            throw new IllegalArgumentException(INVALID_EMAIL);
        }
        value = normalize(value);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Email of(String value) {
        return new Email(value);
    }

    private static boolean isInvalid(String value) {
        return isMissing(value) || isMalformed(normalize(value));
    }

    private static boolean isMalformed(String email) {
        return isLong(email) || !matchesFormat(email);
    }

    private static boolean isMissing(String value) {
        return value == null;
    }

    private static boolean isLong(String email) {
        return email.length() > MAX_LENGTH;
    }

    private static boolean matchesFormat(String email) {
        return FORMAT.matcher(email).matches();
    }

    private static String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}