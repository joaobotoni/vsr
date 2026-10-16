package com.botoni.vsr.ratelimit;

import java.util.concurrent.ConcurrentMap;

final class TokenBucket {

    private static final double NANOS_PER_SECOND = 1_000_000_000.0;

    private final Limit limit;
    private final ConcurrentMap<String, Bucket> buckets;

    TokenBucket(Limit limit, ConcurrentMap<String, Bucket> buckets) {
        this.limit = limit;
        this.buckets = buckets;
    }

    static double now() {
        return System.nanoTime() / NANOS_PER_SECOND;
    }

    Allowance consume(String key) {
        return take(key, now());
    }

    Allowance take(String key, double now) {
        return allowance(buckets.compute(key, (k, bucket) -> refill(bucket, now).consume()), now);
    }

    void refund(String key) {
        buckets.computeIfPresent(key, (k, bucket) -> bucket.refund(limit));
    }

    private Bucket refill(Bucket bucket, double now) {
        return absent(bucket) ? Bucket.full(limit, now) : bucket.refill(limit, now);
    }

    private Allowance allowance(Bucket bucket, double now) {
        return new Allowance(bucket.allowed(), bucket.remaining(), bucket.reset(limit, now));
    }

    private static boolean absent(Bucket bucket)     {
        return bucket == null;
    }
}
