package com.botoni.vsr.vo;

import com.botoni.vsr.exception.custom.InvalidPasswordException;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;

public record Password(String value) {

    private static final int MAX_BYTES = 72;
    private static final int MIN_LENGTH = 8;

    public Password {
        if (isInvalid(value)) {
            throw new InvalidPasswordException();
        }
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Password of(String value) {
        return new Password(value);
    }

    public String encodeWith(PasswordEncoder encoder) {
        return encoder.encode(value);
    }

    private static boolean isInvalid(String value) {
        return isBlank(value) || isTooShort(value) || isTooLong(value);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static boolean isTooShort(String value) {
        return value.length() < MIN_LENGTH;
    }

    private static boolean isTooLong(String value) {
        return value.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES;
    }
}