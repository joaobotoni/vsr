package com.botoni.vsr.lib;

public record Modulo11(int maxWeight) {

    private static final int MODULUS = 11;
    private static final int FIRST_WEIGHT = 2;
    private static final int ASCII_ZERO = 48;

    public boolean isInvalid(String number, int checkDigitIndex) {
        String base = number.substring(0, checkDigitIndex);
        return calculate(base) != value(number, checkDigitIndex);
    }

    public int calculate(String base) {
        int complement = MODULUS - remainder(base);
        return normalize(complement);
    }

    private int remainder(String base) {
        return sum(base) % MODULUS;
    }

    private int sum(String base) {
        int total = 0;
        int weight = FIRST_WEIGHT;
        for (int i = base.length() - 1; i >= 0; i--) {
            total += value(base, i) * weight;
            weight = next(weight);
        }
        return total;
    }

    private int next(int weight) {
        if (weight == maxWeight) {
            return FIRST_WEIGHT;
        }
        return weight + 1;
    }

    private static int normalize(int complement) {
        if (complement >= 10) {
            return 0;
        }
        return complement;
    }

    private static int value(String number, int index) {
        return number.charAt(index) - ASCII_ZERO;
    }
}
