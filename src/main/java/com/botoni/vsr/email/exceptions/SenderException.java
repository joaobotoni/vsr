package com.botoni.vsr.email.exceptions;

public abstract sealed class SenderException extends IllegalArgumentException {

    protected SenderException(String message) {
        super(message);
    }

    public static final class MissingDetails extends SenderException {
        public MissingDetails() {
            super("Os dados do e-mail são obrigatórios.");
        }
    }

    public static final class MissingAttachments extends SenderException {
        public MissingAttachments() {
            super("Informe ao menos um anexo.");
        }
    }
}