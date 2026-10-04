package com.botoni.vsr.shared.vo.exception;

public abstract sealed class EmailException extends IllegalArgumentException {

    protected EmailException(String message) {
        super(message);
    }

    public static final class Missing extends EmailException {
        public Missing() {
            super("O e-mail é obrigatório.");
        }
    }

    public static final class TooLong extends EmailException {
        public TooLong(int max) {
            super(String.format("O e-mail deve conter no máximo %d caracteres.", max));
        }
    }

    public static final class ContainsWhitespace extends EmailException {
        public ContainsWhitespace() {
            super("O e-mail não pode conter espaços em branco.");
        }
    }

    public static final class MissingAtSign extends EmailException {
        public MissingAtSign() {
            super("O e-mail deve conter o caractere '@'.");
        }
    }

    public static final class MultipleAtSigns extends EmailException {
        public MultipleAtSigns() {
            super("O e-mail deve conter apenas um caractere '@'.");
        }
    }

    public static final class LocalPartTooLong extends EmailException {
        public LocalPartTooLong(int max) {
            super(String.format("O nome de usuário do e-mail, antes do '@', deve conter no máximo %d caracteres.", max));
        }
    }

    public static final class InvalidLocalPart extends EmailException {
        public InvalidLocalPart() {
            super("O nome de usuário do e-mail, antes do '@', é inválido.");
        }
    }

    public static final class InvalidDomain extends EmailException {
        public InvalidDomain() {
            super("O domínio do e-mail é inválido.");
        }
    }

    public static final class InvalidTopLevelDomain extends EmailException {
        public InvalidTopLevelDomain() {
            super("A extensão do domínio do e-mail é inválida.");
        }
    }
}