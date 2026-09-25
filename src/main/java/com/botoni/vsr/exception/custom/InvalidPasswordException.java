package com.botoni.vsr.exception.custom;

public class InvalidPasswordException extends DomainException {

    private static final String MESSAGE = "A senha deve ter entre 8 e 72 caracteres";

    public InvalidPasswordException() {
        super(MESSAGE);
    }
}
