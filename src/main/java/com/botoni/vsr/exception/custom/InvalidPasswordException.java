package com.botoni.vsr.exception.custom;

public class InvalidPasswordException extends DomainException {
    public InvalidPasswordException() {
        super("A senha deve ter entre 8 e 72 caracteres");
    }
}
