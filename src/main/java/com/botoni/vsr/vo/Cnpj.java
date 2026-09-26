package com.botoni.vsr.vo;

import com.botoni.vsr.utils.Modulo11;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Locale;
import java.util.regex.Pattern;

public record Cnpj(@JsonValue String value) {

    private static final String INVALID_CNPJ = "CNPJ inválido";
    private static final String NON_ALPHANUMERIC = "[^0-9A-Za-z]";
    private static final Pattern FORMAT = Pattern.compile("^[0-9A-Z]{12}[0-9]{2}$");
    private static final int LENGTH = 14;
    private static final int FIRST_CHECK_DIGIT_INDEX = 12;
    private static final int SECOND_CHECK_DIGIT_INDEX = 13;
    private static final Modulo11 MODULO_11 = new Modulo11(9);

    public Cnpj {
        if (isInvalid(value)) {
            throw new IllegalArgumentException(INVALID_CNPJ);
        }
        value = normalize(value);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Cnpj of(String value) {
        return new Cnpj(value);
    }

    private static boolean isInvalid(String value) {
        return isMissing(value) || hasInvalidCharacters(normalize(value));
    }

    private static boolean hasInvalidCharacters(String cnpj) {
        return hasWrongFormat(cnpj) || hasAllCharactersEqual(cnpj) || hasInvalidCheckDigits(cnpj);
    }

    private static boolean hasInvalidCheckDigits(String cnpj) {
        return !MODULO_11.hasValidCheckDigitAt(cnpj, FIRST_CHECK_DIGIT_INDEX)
                || !MODULO_11.hasValidCheckDigitAt(cnpj, SECOND_CHECK_DIGIT_INDEX);
    }

    private static boolean isMissing(String value) {
        return value == null;
    }

    private static boolean hasWrongFormat(String cnpj) {
        return !FORMAT.matcher(cnpj).matches();
    }

    private static boolean hasAllCharactersEqual(String cnpj) {
        return cnpj.equals(String.valueOf(cnpj.charAt(0)).repeat(LENGTH));
    }

    private static String normalize(String value) {
        return value.replaceAll(NON_ALPHANUMERIC, "").toUpperCase(Locale.ROOT);
    }
}