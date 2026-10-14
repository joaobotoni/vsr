package com.botoni.vsr.attempt;

public record Attempt(String key, Class<? extends RuntimeException> failure) {

    public static Attempt of(String key, Class<? extends RuntimeException> failure) {
        return new Attempt(key, failure);
    }

    public boolean fails(RuntimeException exception) {
        return failure.isInstance(exception);
    }
}