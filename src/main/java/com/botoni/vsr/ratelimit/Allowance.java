package com.botoni.vsr.ratelimit;

public record Allowance(boolean allowed, double remaining, double reset) {

    private static final long MIN_RETRY = 1;

    public boolean exceeded() {
        return !allowed;
    }

    public long retry() {
        return Math.max(MIN_RETRY, rounded());
    }

    private long rounded() {
        return (long) Math.ceil(reset);
    }
}
