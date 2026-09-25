package com.botoni.vsr.vo;

import com.botoni.vsr.exception.custom.DomainException;

import java.util.stream.IntStream;

public record Cpf(String value) {

    private static final String INVALID_CPF = "CPF inválido";
    private static final String NON_DIGITS = "[^0-9]";
    private static final String EMPTY = "";
    private static final int LENGTH = 11;
    private static final int FIRST_CHECK_DIGIT_INDEX = 9;
    private static final int SECOND_CHECK_DIGIT_INDEX = 10;
    private static final int MODULUS = 11;
    private static final int WEIGHT_OFFSET = 1;
    private static final int MIN_REMAINDER_FOR_SUBTRACTION = 2;
    private static final int ZERO_CHECK_DIGIT = 0;
    private static final long SINGLE_DISTINCT_DIGIT = 1;

    public Cpf {
        if (value == null) {
            throw new DomainException(INVALID_CPF);
        }
        if (!isValid(digitsOf(value))) {
            throw new DomainException(INVALID_CPF);
        }
        value = digitsOf(value);
    }

    private static String digitsOf(String rawValue) {
        return rawValue.replaceAll(NON_DIGITS, EMPTY);
    }

    private static boolean isValid(String digits) {
        return hasExpectedLength(digits) && !hasAllDigitsEqual(digits) && hasValidCheckDigits(digits);
    }

    private static boolean hasExpectedLength(String digits) {
        return digits.length() == LENGTH;
    }

    private static boolean hasAllDigitsEqual(String digits) {
        return digits.chars().distinct().count() == SINGLE_DISTINCT_DIGIT;
    }

    private static boolean hasValidCheckDigits(String digits) {
        return isCheckDigitValid(digits, FIRST_CHECK_DIGIT_INDEX) && isCheckDigitValid(digits, SECOND_CHECK_DIGIT_INDEX);
    }

    private static boolean isCheckDigitValid(String digits, int index) {
        return checkDigitOf(digits.substring(0, index)) == Character.getNumericValue(digits.charAt(index));
    }

    private static int checkDigitOf(String base) {
        int remainder = weightedSumOf(base) % MODULUS;
        if (remainder < MIN_REMAINDER_FOR_SUBTRACTION) {
            return ZERO_CHECK_DIGIT;
        }
        return MODULUS - remainder;
    }

    private static int weightedSumOf(String base) {
        return IntStream.range(0, base.length())
                .map(position -> Character.getNumericValue(base.charAt(position)) * weightAt(base, position))
                .sum();
    }

    private static int weightAt(String base, int position) {
        return base.length() - position + WEIGHT_OFFSET;
    }
}
