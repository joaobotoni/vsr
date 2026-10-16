package com.botoni.vsr.vo;

import com.botoni.vsr.exception.custom.NameException;
import com.botoni.vsr.exception.enums.problem.NameProblem;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Nome formado só por palavras de letras, com ou sem acento, separadas por um espaço. Cada acento combinado
 * acompanha uma letra, no máximo dois por letra.
 * Chega normalizado: acentos compostos (NFC), sem espaços nas pontas e com espaços repetidos reduzidos a um.
 */
public record Name(@JsonValue String value) {

    private static final int MAX_LENGTH = 200;
    private static final Pattern SPACES = Pattern.compile("\\s+");
    private static final String WORD = "(\\p{L}\\p{M}{0,2})+";
    private static final Pattern WORDS = Pattern.compile(WORD + "( " + WORD + ")*");
    private static final String SPACE = " ";

    public Name {
        if (value == null) {
            throw new NameException(NameProblem.MISSING);
        }
        value = normalize(value);
        validate(value);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Name of(String value) {
        return new Name(value);
    }

    private static String normalize(String value) {
        String composed = Normalizer.normalize(value, Normalizer.Form.NFC);
        return SPACES.matcher(composed.strip()).replaceAll(SPACE);
    }

    private static void validate(String value) {
        if (value.isEmpty()) {
            throw new NameException(NameProblem.MISSING);
        }
        if (isLong(value)) {
            throw new NameException(NameProblem.TOO_LONG, MAX_LENGTH);
        }
        if (!isWords(value)) {
            throw new NameException(NameProblem.INVALID_CHARACTERS);
        }
    }

    private static boolean isLong(String value) {
        return value.codePointCount(0, value.length()) > MAX_LENGTH;
    }

    private static boolean isWords(String value) {
        return WORDS.matcher(value).matches();
    }
}
