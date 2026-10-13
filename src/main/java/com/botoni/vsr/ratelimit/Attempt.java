package com.botoni.vsr.ratelimit;

public record Attempt(String key, Class<? extends RuntimeException> rejection) {

    public static Attempt of(String key, Class<? extends RuntimeException> rejection) {
        return new Attempt(key, rejection);
    }

    public boolean rejects(RuntimeException exception) {
        return rejection.isInstance(exception);
    }
}
