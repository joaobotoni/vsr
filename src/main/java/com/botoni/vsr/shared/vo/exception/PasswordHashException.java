package com.botoni.vsr.shared.vo.exception;

public abstract sealed class PasswordHashException extends IllegalArgumentException {

    protected PasswordHashException(String message) {
        super(message);
    }

    public static final class Missing extends PasswordHashException {
        public Missing() {
            super("O hash da senha é obrigatório.");
        }
    }

    public static final class TooShort extends PasswordHashException {
        public TooShort(int min) {
            super(String.format("O hash da senha deve conter no mínimo %d caracteres.", min));
        }
    }

    public static final class TooLong extends PasswordHashException {
        public TooLong(int max) {
            super(String.format("O hash da senha deve conter no máximo %d caracteres.", max));
        }
    }

    public static final class ContainsWhitespace extends PasswordHashException {
        public ContainsWhitespace() {
            super("O hash da senha não pode conter espaços em branco.");
        }
    }

    public static final class InvalidCharacters extends PasswordHashException {
        public InvalidCharacters() {
            super("O hash da senha contém caracteres inválidos.");
        }
    }
}