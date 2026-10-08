package com.botoni.vsr.ratelimit;

public record Quota(boolean allowed, double remaining, double resetIn) {

    private static final long MIN_RETRY = 1;

    public boolean exceeded() {
        return !allowed;
    }

    public long retryAfter() {
        return Math.max(MIN_RETRY, (long) Math.ceil(resetIn));
    }
}
