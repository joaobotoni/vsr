package com.botoni.vsr.exception.infrastructure;

public abstract sealed class UnknownTypeException extends RuntimeException {

    protected UnknownTypeException(String message) {
        super(message);
    }

    public static final class Pessoa extends UnknownTypeException {
        public Pessoa(String value) {
            super(String.format("O tipo de pessoa %s é desconhecido.", value));
        }
    }
}