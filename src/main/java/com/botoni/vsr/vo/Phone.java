package com.botoni.vsr.vo;

import com.botoni.vsr.exception.custom.PhoneException;
import com.botoni.vsr.exception.enums.problem.PhoneProblem;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.regex.Pattern;

/**
 * Telefone brasileiro com DDD, guardado só com dígitos: celular com 9 na frente ou fixo, como em
 * {@code documento.telefone_valido}. Aceita a entrada com máscara ({@code (11) 91234-5678}) e com {@code +55}.
 */
public record Phone(@JsonValue String value) {

    private static final String MASK_CHARACTERS = "[()\\s.-]";
    private static final String COUNTRY = "+55";
    private static final Pattern VALID = Pattern.compile("[1-9][1-9](9[0-9]{8}|[2-5][0-9]{7})");

    public Phone {
        if (value == null) {
            throw new PhoneException(PhoneProblem.MISSING);
        }
        value = normalize(value);
        validate(value);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Phone of(String value) {
        return new Phone(value);
    }

    private static String normalize(String value) {
        String digits = value.replaceAll(MASK_CHARACTERS, "");
        return digits.startsWith(COUNTRY) ? digits.substring(COUNTRY.length()) : digits;
    }

    private static void validate(String value) {
        if (value.isEmpty()) {
            throw new PhoneException(PhoneProblem.MISSING);
        }
        if (!VALID.matcher(value).matches()) {
            throw new PhoneException(PhoneProblem.INVALID);
        }
    }
}
