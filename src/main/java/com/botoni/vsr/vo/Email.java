package com.botoni.vsr.vo;

import com.botoni.vsr.exception.custom.DomainException;

import java.util.Locale;
import java.util.regex.Pattern;

public record Email(String value) {

    private static final String INVALID_EMAIL = "E-mail inválido";

    private static final Pattern FORMAT = Pattern.compile("^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$");

    private static final int MIN_LENGTH = 6;

    private static final int MAX_LENGTH = 254;

    public Email {
        if (!isValid(value)) {
            throw new DomainException(INVALID_EMAIL);
        }
        value = normalize(value);
    }

    public static boolean isValid(String rawValue) {
        if (rawValue == null) {
            return false;
        }
        return isWellFormed(normalize(rawValue));
    }

    private static String normalize(String rawValue) {
        return rawValue.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean isWellFormed(String normalizedValue) {
        return hasValidLength(normalizedValue) && FORMAT.matcher(normalizedValue).matches();
    }

    private static boolean hasValidLength(String normalizedValue) {
        return normalizedValue.length() >= MIN_LENGTH && normalizedValue.length() <= MAX_LENGTH;
    }
}
