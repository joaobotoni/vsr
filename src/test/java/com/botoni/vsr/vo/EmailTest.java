package com.botoni.vsr.vo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailTest {

    private static final String NORMALIZED = "ana.souza+vsr@example.com.br";
    private static final String UNNORMALIZED = "  Ana.Souza+VSR@Example.COM.br ";
    private static final String SHORTEST_VALID = "a@b.co";
    private static final String INVALID_EMAIL = "E-mail inválido";
    private static final String DOMAIN_SUFFIX = "@example.com";
    private static final String LOCAL_PART_CHARACTER = "a";
    private static final int MAX_LENGTH = 254;

    @Test
    void trimsAndLowercases() {
        assertEquals(NORMALIZED, new Email(UNNORMALIZED).value());
    }

    @Test
    void equalsByNormalizedValue() {
        assertEquals(new Email(NORMALIZED), new Email(UNNORMALIZED));
    }

    @Test
    void acceptsBoundaryLengths() {
        assertEquals(SHORTEST_VALID, new Email(SHORTEST_VALID).value());
        assertEquals(MAX_LENGTH, new Email(emailWithLength(MAX_LENGTH)).value().length());
    }

//    @Test
//    void reportsValidityWithoutThrowing() {
//        assertTrue(Email.isInvalid(UNNORMALIZED));
//        assertFalse(Email.isInvalid(null));
//        assertFalse(Email.isInvalid(emailWithLength(MAX_LENGTH + 1)));
//    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {
            "",
            "   ",
            "ana",
            "ana@",
            "@example.com",
            "ana@example",
            "ana@example.c",
            "a@b.c",
            "ana souza@example.com",
            "ana@exa mple.com",
            "ana@example.com1",
            "joão@example.com"
    })
    void failsFastOnInvalidInput(String rawValue) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Email(rawValue));
        assertEquals(INVALID_EMAIL, exception.getMessage());
    }

    @Test
    void failsFastWhenTooLong() {
        String tooLong = emailWithLength(MAX_LENGTH + 1);
        assertThrows(IllegalArgumentException.class, () -> new Email(tooLong));
    }

    private static String emailWithLength(int length) {
        return LOCAL_PART_CHARACTER.repeat(length - DOMAIN_SUFFIX.length()) + DOMAIN_SUFFIX;
    }
}
