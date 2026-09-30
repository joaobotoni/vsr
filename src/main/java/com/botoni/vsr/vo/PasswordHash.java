package com.botoni.vsr.vo;

import lombok.NonNull;
import java.util.regex.Pattern;
import org.springframework.security.crypto.password.PasswordEncoder;

public record PasswordHash(String value) {

    private static final int MIN_LENGTH = 20;
    private static final int MAX_LENGTH = 255;

    private static final Pattern WHITESPACE = Pattern.compile("\\s");

    private static final String INVALID_MESSAGE = "Credencial inválida";

    public PasswordHash {
        if (isInvalid(value)) {
            throw new IllegalArgumentException(INVALID_MESSAGE);
        }
    }

    public static PasswordHash of(String value) {
        return new PasswordHash(value);
    }

    public static PasswordHash encode(Password password, PasswordEncoder encoder) {
        return new PasswordHash(encoder.encode(password.value()));
    }

    public boolean matches(Password password, PasswordEncoder encoder) {
        return encoder.matches(password.value(), value);
    }

    public boolean needsRehash(PasswordEncoder encoder) {
        return encoder.upgradeEncoding(value);
    }

    private static boolean isInvalid(String value) {
        return isMissing(value) || isMalformed(value);
    }

    private static boolean isMalformed(String value) {
        return isShort(value) || isLong(value) || contains(value);
    }

    private static boolean isMissing(String value) {
        return value == null || value.isBlank();
    }

    private static boolean isShort(String value) {
        return value.length() < MIN_LENGTH;
    }

    private static boolean isLong(String value) {
        return value.length() > MAX_LENGTH;
    }

    private static boolean contains(String value) {
        return PasswordHash.WHITESPACE.matcher(value).find();
    }

    @Override
    @NonNull
    public String toString() {
        return "****";
    }
}
