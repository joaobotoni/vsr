package com.botoni.vsr.ratelimit;

public record Quota(boolean allowed, double remaining, double reset) {

    private static final long MIN_RETRY = 1;

    public boolean isExceeded() {
        return !allowed;
    }

    public long retry() {
        return Math.max(MIN_RETRY, (long) Math.ceil(reset));
    }
}
