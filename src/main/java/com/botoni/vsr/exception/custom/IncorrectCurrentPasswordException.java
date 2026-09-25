package com.botoni.vsr.exception.custom;

public class IncorrectCurrentPasswordException extends DomainException {
    public IncorrectCurrentPasswordException() {
        super("Senha atual incorreta");
    }
}
