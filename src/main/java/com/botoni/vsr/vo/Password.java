package com.botoni.vsr.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import lombok.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;

public record Password(String value) {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_BYTES = 72;

    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[^A-Za-z0-9\\s]");
    private static final Pattern WHITESPACE = Pattern.compile("\\s");

    private static final String REQUIRED_MESSAGE = "A senha é obrigatória";
    private static final String TOO_SHORT_MESSAGE = "A senha deve ter no mínimo 8 caracteres";
    private static final String TOO_LONG_MESSAGE = "A senha é longa demais";
    private static final String UPPERCASE_MESSAGE = "A senha deve ter pelo menos uma letra maiúscula";
    private static final String LOWERCASE_MESSAGE = "A senha deve ter pelo menos uma letra minúscula";
    private static final String DIGIT_MESSAGE = "A senha deve ter pelo menos um número";
    private static final String SPECIAL_MESSAGE = "A senha deve ter pelo menos um caractere especial";
    private static final String WHITESPACE_MESSAGE = "A senha não pode conter espaços";

    public Password {
        if (isMissing(value)) {
            throw new IllegalArgumentException(REQUIRED_MESSAGE);
        }
        if (isShort(value)) {
            throw new IllegalArgumentException(TOO_SHORT_MESSAGE);
        }
        if (isLong(value)) {
            throw new IllegalArgumentException(TOO_LONG_MESSAGE);
        }
        if (lacks(UPPERCASE, value)) {
            throw new IllegalArgumentException(UPPERCASE_MESSAGE);
        }
        if (lacks(LOWERCASE, value)) {
            throw new IllegalArgumentException(LOWERCASE_MESSAGE);
        }
        if (lacks(DIGIT, value)) {
            throw new IllegalArgumentException(DIGIT_MESSAGE);
        }
        if (lacks(SPECIAL, value)) {
            throw new IllegalArgumentException(SPECIAL_MESSAGE);
        }
        if (contains(WHITESPACE, value)) {
            throw new IllegalArgumentException(WHITESPACE_MESSAGE);
        }
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Password of(String value) {
        return new Password(value);
    }

    public String encodeWith(PasswordEncoder encoder) {
        return encoder.encode(value);
    }

    private static boolean isMissing(String value) {
        return value == null || value.isBlank();
    }

    private static boolean isShort(String value) {
        return value.length() < MIN_LENGTH;
    }

    private static boolean isLong(String value) {
        return value.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES;
    }

    private static boolean lacks(Pattern pattern, String value) {
        return !contains(pattern, value);
    }

    private static boolean contains(Pattern pattern, String value) {
        return pattern.matcher(value).find();
    }

    @Override
    @NonNull
    public String toString() {
        return "****";
    }
}