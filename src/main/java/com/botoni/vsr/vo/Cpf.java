package com.botoni.vsr.vo;

import com.botoni.vsr.exception.custom.CpfException;
import com.botoni.vsr.exception.enums.problem.CpfProblem;
import com.botoni.vsr.lib.Modulo11;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.regex.Pattern;

public record Cpf(@JsonValue String value) {

    private static final String MASK_CHARACTERS = "[./\\s-]";
    private static final Pattern NUMERIC = Pattern.compile("[0-9]+");
    private static final int LENGTH = 11;
    private static final int FIRST_CHECK_DIGIT_INDEX = 9;
    private static final int SECOND_CHECK_DIGIT_INDEX = 10;
    private static final Modulo11 MODULO_11 = new Modulo11(11);

    public Cpf {
        if (value == null) {
            throw new CpfException(CpfProblem.MISSING);
        }
        value = normalize(value);
        validate(value);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Cpf of(String value) {
        return new Cpf(value);
    }

    private static void validate(String value) {
        if (value.isEmpty()) {
            throw new CpfException(CpfProblem.MISSING);
        }
        if (isMalformed(value)) {
            throw new CpfException(CpfProblem.LENGTH, LENGTH);
        }
        if (!isNumeric(value)) {
            throw new CpfException(CpfProblem.CHARACTERS);
        }
        if (isRepeated(value)) {
            throw new CpfException(CpfProblem.REPEATED_DIGITS);
        }
        if (isMismatched(value)) {
            throw new CpfException(CpfProblem.CHECK_DIGITS);
        }
    }

    private static boolean isMalformed(String value) {
        return value.length() != LENGTH;
    }

    private static boolean isNumeric(String value) {
        return NUMERIC.matcher(value).matches();
    }

    private static boolean isRepeated(String value) {
        return value.chars().allMatch(c -> c == value.charAt(0));
    }

    private static boolean isMismatched(String value) {
        return MODULO_11.isInvalid(value, FIRST_CHECK_DIGIT_INDEX)
                || MODULO_11.isInvalid(value, SECOND_CHECK_DIGIT_INDEX);
    }

    private static String normalize(String value) {
        return value.replaceAll(MASK_CHARACTERS, "");
    }
}
