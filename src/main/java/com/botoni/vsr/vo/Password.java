package com.botoni.vsr.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.nio.charset.StandardCharsets;
import org.springframework.security.crypto.password.PasswordEncoder;

public record Password(String value) {

    private static final int MIN_LENGTH = 8;
    private static final int BCRYPT_MAX_BYTES = 72;
    private static final String INVALID_PASSWORD = "A senha deve ter no mínimo 8 caracteres";

    public Password {
        if (isInvalid(value)) {
            throw new IllegalArgumentException(INVALID_PASSWORD);
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
        return isMissing(value) || isBlank(value) || isShort(value) || isLong(value);
    }

    private static boolean isMissing(String value) {
        return value == null;
    }

    private static boolean isBlank(String value) {
        return value.isBlank();
    }

    private static boolean isShort(String value) {
        return value.length() < MIN_LENGTH;
    }

    private static boolean isLong(String value) {
        return value.getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_BYTES;
    }
}