package com.botoni.vsr.vo;

import com.botoni.vsr.exception.custom.CnpjException;
import com.botoni.vsr.exception.enums.problem.CnpjProblem;
import com.botoni.vsr.lib.Modulo11;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Locale;
import java.util.regex.Pattern;

public record Cnpj(@JsonValue String value) {

    private static final String MASK_CHARACTERS = "[./\\s-]";
    private static final Pattern ALPHANUMERIC = Pattern.compile("[0-9A-Z]+");
    private static final Pattern NUMERIC = Pattern.compile("[0-9]+");
    private static final int LENGTH = 14;
    private static final int BASE_LENGTH = 12;
    private static final int FIRST_CHECK_DIGIT_INDEX = 12;
    private static final int SECOND_CHECK_DIGIT_INDEX = 13;
    private static final Modulo11 MODULO_11 = new Modulo11(9);

    public Cnpj {
        if (value == null) {
            throw new CnpjException(CnpjProblem.MISSING);
        }
        value = normalize(value);
        validate(value);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Cnpj of(String value) {
        return new Cnpj(value);
    }

    private static void validate(String value) {
        if (value.isEmpty()) {
            throw new CnpjException(CnpjProblem.MISSING);
        }
        if (isMalformed(value)) {
            throw new CnpjException(CnpjProblem.LENGTH, LENGTH);
        }
        if (!isAlphanumeric(value)) {
            throw new CnpjException(CnpjProblem.CHARACTERS);
        }
        if (!isNumeric(verifier(value))) {
            throw new CnpjException(CnpjProblem.NON_NUMERIC_CHECK_DIGITS);
        }
        if (isRepeated(value)) {
            throw new CnpjException(CnpjProblem.REPEATED_CHARACTERS);
        }
        if (isMismatched(value)) {
            throw new CnpjException(CnpjProblem.CHECK_DIGITS);
        }
    }

    private static boolean isMalformed(String value) {
        return value.length() != LENGTH;
    }

    private static boolean isAlphanumeric(String value) {
        return ALPHANUMERIC.matcher(value).matches();
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

    private static String verifier(String value) {
        return value.substring(BASE_LENGTH);
    }

    private static String normalize(String value) {
        return value.toUpperCase(Locale.ROOT).replaceAll(MASK_CHARACTERS, "");
    }
}
