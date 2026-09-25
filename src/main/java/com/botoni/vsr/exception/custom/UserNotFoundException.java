package com.botoni.vsr.exception.custom;

public class UserNotFoundException extends DomainException {

    public UserNotFoundException() {
        super("Usuário não encontrado");
    }
}
