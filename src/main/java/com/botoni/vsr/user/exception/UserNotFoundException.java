package com.botoni.vsr.user.exception;

public final class UserNotFoundException extends RuntimeException {

    public UserNotFoundException() {
        super("O usuário solicitado não foi encontrado.");
    }
}
