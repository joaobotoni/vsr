package com.botoni.vsr.vo;

import com.botoni.vsr.exception.custom.PasswordException;
import com.botoni.vsr.exception.enums.problem.PasswordProblem;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.nio.charset.StandardCharsets;

/**
 * Senha já existente, como chega no login e na senha atual da troca. Só garante que existe e que cabe
 * no hash; as regras de criação ficam em {@link NewPassword}, para que uma regra nova nunca tranque
 * quem criou a senha antes dela.
 */
public record Password(@JsonValue String value) {

    private static final int MAX_BYTES = 72;

    public Password {
        if (value == null) {
            throw new PasswordException(PasswordProblem.MISSING);
        }
        validate(value);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Password of(String value) {
        return new Password(value);
    }

    private static void validate(String value) {
        if (value.isBlank()) {
            throw new PasswordException(PasswordProblem.MISSING);
        }
        if (isLong(value)) {
            throw new PasswordException(PasswordProblem.TOO_LONG);
        }
    }

    private static boolean isLong(String value) {
        return value.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES;
    }

    @Override
    public String toString() {
        return "****";
    }
}
