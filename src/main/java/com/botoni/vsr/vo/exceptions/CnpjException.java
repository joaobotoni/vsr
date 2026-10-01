package com.botoni.vsr.vo.exceptions;

public abstract sealed class CnpjException extends IllegalArgumentException {

    protected CnpjException(String message) {
        super(message);
    }

    public static final class Missing extends CnpjException {
        public Missing() {
            super("O CNPJ é obrigatório.");
        }
    }

    public static final class Length extends CnpjException {
        public Length(int expected) {
            super(String.format("O CNPJ deve conter exatamente %d caracteres.", expected));
        }
    }

    public static final class Characters extends CnpjException {
        public Characters() {
            super("O CNPJ deve conter apenas letras maiúsculas e números.");
        }
    }

    public static final class NonNumericCheckDigits extends CnpjException {
        public NonNumericCheckDigits() {
            super("Os dígitos verificadores do CNPJ devem ser numéricos.");
        }
    }

    public static final class RepeatedCharacters extends CnpjException {
        public RepeatedCharacters() {
            super("O CNPJ informado é inválido, pois todos os caracteres são iguais.");
        }
    }

    public static final class CheckDigits extends CnpjException {
        public CheckDigits() {
            super("O CNPJ informado é inválido, pois os dígitos verificadores não conferem.");
        }
    }
}