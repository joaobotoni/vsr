package com.botoni.vsr.person.exception;

public final class UnknownPersonTypeException extends RuntimeException {

    public UnknownPersonTypeException(String value) {
        super(String.format("O tipo de pessoa %s é desconhecido.", value));
    }
}
