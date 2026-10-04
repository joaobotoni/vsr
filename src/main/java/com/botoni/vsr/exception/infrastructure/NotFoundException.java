package com.botoni.vsr.exception.infrastructure;

public abstract sealed class NotFoundException extends RuntimeException {

    protected NotFoundException(String message) {
        super(message);
    }

    public static final class Usuario extends NotFoundException {
        public Usuario() {
            super("O usuário solicitado não foi encontrado.");
        }
    }

}
