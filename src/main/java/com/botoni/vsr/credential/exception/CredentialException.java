package com.botoni.vsr.credential.exception;

public abstract sealed class CredentialException extends RuntimeException {

    protected CredentialException(String message) {
        super(message);
    }

    public static final class IncorrectCurrentPassword extends CredentialException {
        public IncorrectCurrentPassword() {
            super("A senha atual informada está incorreta.");
        }
    }
}
