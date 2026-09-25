package com.botoni.vsr.domain.vo;

import com.botoni.vsr.exception.custom.InvalidPasswordException;
import com.botoni.vsr.vo.Password;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PasswordTest {

    private static final String CHARACTER = "a";
    private static final String INVALID_PASSWORD = "A senha deve ter entre 8 e 72 caracteres";

    @ParameterizedTest
    @NullAndEmptySource
    void rejectsNullAndEmpty(String rawValue) {
        InvalidPasswordException exception = assertThrows(InvalidPasswordException.class, () -> new Password(rawValue));
        assertEquals(INVALID_PASSWORD, exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(ints = {7, 73})
    void rejectsLengthOutsideLimits(int length) {
        assertThrows(InvalidPasswordException.class, () -> Password.of(ofLength(length)));
    }

    @ParameterizedTest
    @ValueSource(ints = {8, 72})
    void acceptsLengthAtLimits(int length) {
        assertEquals(ofLength(length), Password.of(ofLength(length)).value());
    }

    private static String ofLength(int length) {
        return CHARACTER.repeat(length);
    }
}
