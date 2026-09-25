package com.botoni.vsr.exception.custom;

public class UserNotFoundException extends DomainException {

    private static final String MESSAGE = "Usuário não encontrado";

    public UserNotFoundException() {
        super(MESSAGE);
    }
}
