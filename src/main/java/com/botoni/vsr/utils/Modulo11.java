package com.botoni.vsr.utils;

public record Modulo11(int maxWeight) {

    private static final int MODULUS = 11;
    private static final int MIN_WEIGHT = 2;
    private static final int MIN_REMAINDER_FOR_SUBTRACTION = 2;
    private static final int ZERO_CHECK_DIGIT = 0;
    private static final char ZERO = '0';

    public boolean hasValidCheckDigitAt(String value, int index) {
        int expected = checkDigitOf(baseOf(value, index));
        int actual = numericValueAt(value, index);
        return expected == actual;
    }

    public int checkDigitOf(String base) {
        return checkDigitFromRemainder(remainderOf(base));
    }

    private static int checkDigitFromRemainder(int remainder) {
        if (resultsInZero(remainder)) {
            return ZERO_CHECK_DIGIT;
        }
        return MODULUS - remainder;
    }

    private static boolean resultsInZero(int remainder) {
        return remainder < MIN_REMAINDER_FOR_SUBTRACTION;
    }

    private int remainderOf(String base) {
        return weightedSumOf(base) % MODULUS;
    }

    private int weightedSumOf(String base) {
        int sum = 0;
        for (int position = 0; position < base.length(); position++) {
            sum += weightedValueAt(base, position);
        }
        return sum;
    }

    private int weightedValueAt(String base, int position) {
        return numericValueAt(base, position) * weightAt(base, position);
    }

    private int weightAt(String base, int position) {
        int distanceFromEnd = base.length() - 1 - position;
        return distanceFromEnd % weightCycleLength() + MIN_WEIGHT;
    }

    private int weightCycleLength() {
        return maxWeight - MIN_WEIGHT + 1;
    }

    private static String baseOf(String value, int length) {
        return value.substring(0, length);
    }

    private static int numericValueAt(String value, int index) {
        return value.charAt(index) - ZERO;
    }
}