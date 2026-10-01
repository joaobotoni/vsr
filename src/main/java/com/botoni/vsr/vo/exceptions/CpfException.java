package com.botoni.vsr.vo.exceptions;

public abstract sealed class CpfException extends IllegalArgumentException {

    protected CpfException(String message) {
        super(message);
    }

    public static final class Missing extends CpfException {
        public Missing() {
            super("O CPF é obrigatório.");
        }
    }

    public static final class Length extends CpfException {
        public Length(int expected) {
            super(String.format("O CPF deve conter exatamente %d dígitos.", expected));
        }
    }

    public static final class Characters extends CpfException {
        public Characters() {
            super("O CPF deve conter apenas dígitos numéricos.");
        }
    }

    public static final class RepeatedDigits extends CpfException {
        public RepeatedDigits() {
            super("O CPF informado é inválido, pois todos os dígitos são iguais.");
        }
    }

    public static final class CheckDigits extends CpfException {
        public CheckDigits() {
            super("O CPF informado é inválido, pois os dígitos verificadores não conferem.");
        }
    }
}