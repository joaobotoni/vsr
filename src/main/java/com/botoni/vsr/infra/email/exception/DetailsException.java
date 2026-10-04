package com.botoni.vsr.infra.email.exception;

public abstract sealed class DetailsException extends IllegalArgumentException {

    protected DetailsException(String message) {
        super(message);
    }

    public static final class MissingSender extends DetailsException {
        public MissingSender() {
            super("O remetente do e-mail é obrigatório.");
        }
    }

    public static final class MissingRecipient extends DetailsException {
        public MissingRecipient() {
            super("O destinatário do e-mail é obrigatório.");
        }
    }

    public static final class MissingSubject extends DetailsException {
        public MissingSubject() {
            super("O assunto do e-mail é obrigatório.");
        }
    }

    public static final class SubjectTooLong extends DetailsException {
        public SubjectTooLong(int max) {
            super(String.format("O assunto do e-mail deve conter no máximo %d caracteres.", max));
        }
    }

    public static final class MissingBody extends DetailsException {
        public MissingBody() {
            super("O corpo do e-mail é obrigatório.");
        }
    }
}