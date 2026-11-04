package com.botoni.vsr.vo;

import com.botoni.vsr.exception.custom.CepException;
import com.botoni.vsr.exception.enums.problem.CepProblem;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.regex.Pattern;

/**
 * CEP com 8 dígitos, guardado sem máscara. Aceita a entrada com hífen, ponto ou espaços ({@code 01310-100}).
 */
public record Cep(@JsonValue String value) {

    private static final String MASK_CHARACTERS = "[.\\s-]";
    private static final int LENGTH = 8;
    private static final Pattern DIGITS = Pattern.compile("[0-9]{" + LENGTH + "}");
    private static final String ZEROS = "0".repeat(LENGTH);

    public Cep {
        if (value == null) {
            throw new CepException(CepProblem.MISSING);
        }
        value = normalize(value);
        validate(value);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Cep of(String value) {
        return new Cep(value);
    }

    private static String normalize(String value) {
        return value.replaceAll(MASK_CHARACTERS, "");
    }

    private static void validate(String value) {
        if (value.isEmpty()) {
            throw new CepException(CepProblem.MISSING);
        }
        if (!isDigits(value)) {
            throw new CepException(CepProblem.FORMAT, LENGTH);
        }
        if (ZEROS.equals(value)) {
            throw new CepException(CepProblem.ZEROS);
        }
    }

    private static boolean isDigits(String value) {
        return DIGITS.matcher(value).matches();
    }
}
