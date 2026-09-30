package com.botoni.vsr.vo;

import com.botoni.vsr.utils.Modulo11;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.regex.Pattern;

public record Cpf(@JsonValue String value) {

    private static final String INVALID_MESSAGE = "CPF inválido";
    private static final String IGNORED_CHARACTERS = "[^0-9]";
    private static final Pattern FORMAT = Pattern.compile("^[0-9]{11}$");
    private static final int FIRST_CHECK_DIGIT_INDEX = 9;
    private static final int SECOND_CHECK_DIGIT_INDEX = 10;
    private static final Modulo11 MODULO_11 = new Modulo11(11);

    public Cpf {
        if (value == null) {
            throw new IllegalArgumentException(INVALID_MESSAGE);
        }
        value = normalize(value);
        if (isInvalid(value)) {
            throw new IllegalArgumentException(INVALID_MESSAGE);
        }
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Cpf of(String value) {
        return new Cpf(value);
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