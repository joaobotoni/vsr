package com.botoni.vsr.attempt;

public record Attempt(String key, String ceiling, Class<? extends RuntimeException> failure) {

    private static final String UNCEILED = null;

    public static Attempt of(String key, Class<? extends RuntimeException> failure) {
        return new Attempt(key, UNCEILED, failure);
    }

    public static Attempt ceiled(String key, String ceiling, Class<? extends RuntimeException> failure) {
        present(ceiling);
        return new Attempt(key, ceiling, failure);
    }

    public boolean fails(RuntimeException exception) {
        return failure.isInstance(exception);
    }

    public boolean limited() {
        return ceiling != UNCEILED;
    }

    private static void present(String ceiling) {
        if (!filled(ceiling)) {
            throw new IllegalArgumentException("A chave de teto é obrigatória; sem teto, use Attempt.of");
        }
    }

    private static boolean filled(String ceiling) {
        return ceiling != null && !ceiling.isBlank();
    }
}
