package com.botoni.vsr.vo;

import com.botoni.vsr.utils.Modulo11;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public record Cpf(@JsonValue String value) {

    private static final String INVALID_CPF = "CPF inválido";
    private static final String NON_DIGITS = "[^0-9]";
    private static final int LENGTH = 11;
    private static final int FIRST_CHECK_DIGIT_INDEX = 9;
    private static final int SECOND_CHECK_DIGIT_INDEX = 10;
    private static final Modulo11 MODULO_11 = new Modulo11(11);

    public Cpf {
        if (isInvalid(value)) {
            throw new IllegalArgumentException(INVALID_CPF);
        }
        value = digitsOf(value);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Cpf of(String value) {
        return new Cpf(value);
    }

    private static boolean isInvalid(String value) {
        return isMissing(value) || hasInvalidDigits(digitsOf(value));
    }

    private static boolean hasInvalidDigits(String digits) {
        return hasWrongLength(digits) || hasAllDigitsEqual(digits) || hasInvalidCheckDigits(digits);
    }

    private static boolean hasInvalidCheckDigits(String digits) {
        return !MODULO_11.hasValidCheckDigitAt(digits, FIRST_CHECK_DIGIT_INDEX)
                || !MODULO_11.hasValidCheckDigitAt(digits, SECOND_CHECK_DIGIT_INDEX);
    }

    private static boolean isMissing(String value) {
        return value == null;
    }

    private static boolean hasWrongLength(String digits) {
        return digits.length() != LENGTH;
    }

    private static boolean hasAllDigitsEqual(String digits) {
        return digits.equals(String.valueOf(digits.charAt(0)).repeat(LENGTH));
    }

    private static String digitsOf(String value) {
        return value.replaceAll(NON_DIGITS, "");
    }
}