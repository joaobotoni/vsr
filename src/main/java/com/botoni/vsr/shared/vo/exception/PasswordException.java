package com.botoni.vsr.shared.vo.exception;

public abstract sealed class PasswordException extends IllegalArgumentException {

    protected PasswordException(String message) {
        super(message);
    }

    public static final class Missing extends PasswordException {
        public Missing() {
            super("A senha é obrigatória.");
        }
    }

    public static final class TooShort extends PasswordException {
        public TooShort(int min) {
            super(String.format("A senha deve conter no mínimo %d caracteres.", min));
        }
    }

    public static final class TooLong extends PasswordException {
        public TooLong() {
            super("A senha excede o tamanho máximo permitido.");
        }
    }

    public static final class ContainsWhitespace extends PasswordException {
        public ContainsWhitespace() {
            super("A senha não pode conter espaços em branco.");
        }
    }

    public static final class MissingUppercase extends PasswordException {
        public MissingUppercase() {
            super("A senha deve conter ao menos uma letra maiúscula.");
        }
    }

    public static final class MissingLowercase extends PasswordException {
        public MissingLowercase() {
            super("A senha deve conter ao menos uma letra minúscula.");
        }
    }

    public static final class MissingDigit extends PasswordException {
        public MissingDigit() {
            super("A senha deve conter ao menos um dígito numérico.");
        }
    }

    public static final class MissingSpecialCharacter extends PasswordException {
        public MissingSpecialCharacter() {
            super("A senha deve conter ao menos um caractere especial.");
        }
    }
}