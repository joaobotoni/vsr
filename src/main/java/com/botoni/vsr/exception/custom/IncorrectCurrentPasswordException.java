package com.botoni.vsr.exception.custom;

public class IncorrectCurrentPasswordException extends DomainException {

    private static final String MESSAGE = "Senha atual incorreta";

    public IncorrectCurrentPasswordException() {
        super(MESSAGE);
    }
}
