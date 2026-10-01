package com.botoni.vsr.vo;

import com.botoni.vsr.utils.Modulo11;
import com.botoni.vsr.vo.exceptions.CpfException;
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
            throw new CpfException.Missing();
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
            throw new CpfException.Missing();
        }
        if (hasWrongLength(value)) {
            throw new CpfException.Length(LENGTH);
        }
        if (hasNonNumericCharacters(value)) {
            throw new CpfException.Characters();
        }
        if (hasRepeatedDigits(value)) {
            throw new CpfException.RepeatedDigits();
        }
        if (hasWrongCheckDigits(value)) {
            throw new CpfException.CheckDigits();
        }
    }

    private static boolean hasWrongLength(String value) {
        return value.length() != LENGTH;
    }

    private static boolean hasNonNumericCharacters(String value) {
        return !NUMERIC.matcher(value).matches();
    }

    private static boolean hasRepeatedDigits(String value) {
        return value.chars().allMatch(c -> c == value.charAt(0));
    }

    private static boolean hasWrongCheckDigits(String value) {
        return MODULO_11.isInvalid(value, FIRST_CHECK_DIGIT_INDEX)
                || MODULO_11.isInvalid(value, SECOND_CHECK_DIGIT_INDEX);
    }

    private static String normalize(String value) {
        return value.replaceAll(MASK_CHARACTERS, "");
    }
}