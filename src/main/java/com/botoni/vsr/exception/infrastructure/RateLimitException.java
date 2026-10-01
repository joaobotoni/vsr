package com.botoni.vsr.exception.infrastructure;

public abstract sealed class RateLimitException extends RuntimeException {

    protected RateLimitException(String message) {
        super(message);
    }

    public static final class Exceeded extends RateLimitException {
        public Exceeded(long retryAfterSeconds) {
            super(String.format("O limite de requisições foi excedido. Tente novamente em %d segundos.", retryAfterSeconds));
        }
    }

    public static final class InvalidConfiguration extends RateLimitException {
        public InvalidConfiguration() {
            super("A configuração do limite de requisições é inválida. Os valores devem ser maiores que zero.");
        }
    }
}
