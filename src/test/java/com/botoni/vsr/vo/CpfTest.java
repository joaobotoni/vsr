package com.botoni.vsr.vo;

import com.botoni.vsr.exception.custom.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CpfTest {

    private static final String VALID_DIGITS = "52998224725";
    private static final String VALID_FORMATTED = "529.982.247-25";
    private static final String VALID_WITH_ZERO_CHECK_DIGIT = "10000000108";
    private static final String INVALID_CPF = "CPF inválido";

    @Test
    void keepsOnlyDigits() {
        assertEquals(VALID_DIGITS, new Cpf(VALID_FORMATTED).value());
    }

    @Test
    void acceptsDigitsOnlyInput() {
        assertEquals(VALID_DIGITS, new Cpf(VALID_DIGITS).value());
    }

    @Test
    void acceptsCheckDigitComputedAsZero() {
        assertEquals(VALID_WITH_ZERO_CHECK_DIGIT, new Cpf(VALID_WITH_ZERO_CHECK_DIGIT).value());
    }

    @Test
    void equalsByNormalizedValue() {
        assertEquals(new Cpf(VALID_DIGITS), new Cpf(VALID_FORMATTED));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {
            "",
            "   ",
            "abc",
            "5299822472",
            "529982247250",
            "00000000000",
            "11111111111",
            "52998224715",
            "52998224724",
            "529.982.247-26"
    })
    void failsFastOnInvalidInput(String rawValue) {
        DomainException exception = assertThrows(DomainException.class, () -> new Cpf(rawValue));
        assertEquals(INVALID_CPF, exception.getMessage());
    }
}
