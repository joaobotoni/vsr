package com.botoni.vsr.vo;

import com.botoni.vsr.utils.Modulo11;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.regex.Pattern;

public record Cnpj(@JsonValue String value) {

    private static final String INVALID_MESSAGE = "CNPJ inválido";
    private static final String IGNORED_CHARACTERS = "[^0-9A-Z]";
    private static final Pattern FORMAT = Pattern.compile("^[0-9A-Z]{12}[0-9]{2}$");
    private static final int FIRST_CHECK_DIGIT_INDEX = 12;
    private static final int SECOND_CHECK_DIGIT_INDEX = 13;
    private static final Modulo11 MODULO_11 = new Modulo11(9);

    public Cnpj {
        if (value == null) {
            throw new IllegalArgumentException(INVALID_MESSAGE);
        }
        value = normalize(value);
        if (isInvalid(value)) {
            throw new IllegalArgumentException(INVALID_MESSAGE);
        }
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Cnpj of(String value) {
        return new Cnpj(value);
    }

    private static boolean isInvalid(String value) {
        return hasWrongFormat(value) || hasRepeatedCharacters(value) || hasWrongDigits(value);
    }

    private static boolean hasWrongFormat(String value) {
        return !FORMAT.matcher(value).matches();
    }

    private static boolean hasRepeatedCharacters(String value) {
        char first = value.charAt(0);
        for (int i = 1; i < value.length(); i++) {
            if (value.charAt(i) != first) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasWrongDigits(String value) {
        return MODULO_11.isInvalid(value, FIRST_CHECK_DIGIT_INDEX)
                || MODULO_11.isInvalid(value, SECOND_CHECK_DIGIT_INDEX);
    }

    private static String normalize(String value) {
        return value.replaceAll(IGNORED_CHARACTERS, "");
    }
}