package com.botoni.vsr.shared.vo;

import com.botoni.vsr.shared.lib.Modulo11;
import com.botoni.vsr.shared.vo.exception.CnpjException;
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
            throw new CnpjException.Missing();
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
            throw new CnpjException.Missing();
        }
        if (hasWrongLength(value)) {
            throw new CnpjException.Length(LENGTH);
        }
        if (hasNonAlphanumericCharacters(value)) {
            throw new CnpjException.Characters();
        }
        if (hasNonNumericCheckDigits(value)) {
            throw new CnpjException.NonNumericCheckDigits();
        }
        if (hasRepeatedCharacters(value)) {
            throw new CnpjException.RepeatedCharacters();
        }
        if (hasWrongCheckDigits(value)) {
            throw new CnpjException.CheckDigits();
        }
    }

    private static boolean hasWrongLength(String value) {
        return value.length() != LENGTH;
    }

    private static boolean hasNonAlphanumericCharacters(String value) {
        return !ALPHANUMERIC.matcher(value).matches();
    }

    private static boolean hasNonNumericCheckDigits(String value) {
        return !NUMERIC.matcher(checkDigits(value)).matches();
    }

    private static boolean hasRepeatedCharacters(String value) {
        return value.chars().allMatch(c -> c == value.charAt(0));
    }

    private static boolean hasWrongCheckDigits(String value) {
        return MODULO_11.isInvalid(value, FIRST_CHECK_DIGIT_INDEX)
                || MODULO_11.isInvalid(value, SECOND_CHECK_DIGIT_INDEX);
    }

    private static String checkDigits(String value) {
        return value.substring(BASE_LENGTH);
    }

    private static String normalize(String value) {
        return value.toUpperCase(Locale.ROOT).replaceAll(MASK_CHARACTERS, "");
    }
}