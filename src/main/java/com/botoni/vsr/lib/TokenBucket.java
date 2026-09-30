package com.botoni.vsr.lib;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class TokenBucket implements AutoCloseable {

    public record RateLimitResult(boolean allowed, double remaining) {}
    private record Bucket(double tokens, double lastRefill) {}

    private static final long MIN_PERIOD = 1000L;
    private static final String CLEANER_THREAD_NAME = "token-bucket-cleaner";

    private final int capacity;
    private final double refillRate;
    private final double refillInterval;
    private final double ttl;
    private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleaner = cleaner();

    public TokenBucket(int capacity, double refillRate, double refillInterval) {
        if (capacity <= 0 || refillRate <= 0 || refillInterval <= 0) {
            throw new IllegalArgumentException("Invalid argument: values must be greater than zero.");
        }
        this.capacity = capacity;
        this.refillRate = refillRate;
        this.refillInterval = refillInterval;
        this.ttl = ttl();
        schedule();
    }

    public RateLimitResult allow(String key) {
        return allow(key, now());
    }

    public RateLimitResult allow(String key, double now) {
        long[] result = new long[2];

        buckets.compute(key, (k, bucket) -> {
            double tokens = bucket == null ? capacity : bucket.tokens();
            double lastRefill = bucket == null ? now : bucket.lastRefill();

            double timePassed = now - lastRefill;
            double refills = Math.floor(timePassed / refillInterval);

            if (refills > 0) {
                tokens = Math.min(capacity, tokens + (refills * refillRate));
                lastRefill = lastRefill + (refills * refillInterval);
            }

            long allowed = 0;
            if (tokens >= 1) {
                tokens = tokens - 1;
                allowed = 1;
            }

            result[0] = allowed;
            result[1] = (long) tokens;

            return new Bucket(tokens, lastRefill);
        });

        return new RateLimitResult(result[0] == 1L, result[1]);
    }

    void evict(double now) {
        buckets.values().removeIf(b -> idle(b, now));
    }

    @Override
    public void close() {
        cleaner.shutdownNow();
        buckets.clear();
    }

    private double ttl() {
        return Math.ceil(capacity / refillRate) * refillInterval;
    }

    private long period() {
        return Math.max(MIN_PERIOD, (long) (ttl * 1000));
    }

    private boolean idle(Bucket b, double now) {
        return now - b.lastRefill() >= ttl;
    }

    private void schedule() {
        long p = period();
        cleaner.scheduleAtFixedRate(() -> evict(now()), p, p, TimeUnit.MILLISECONDS);
    }

    private static double now() {
        return System.currentTimeMillis() / 1000.0;
    }

    private static ScheduledExecutorService cleaner() {
        return Executors.newSingleThreadScheduledExecutor(daemon());
    }

    private static ThreadFactory daemon() {
        return r -> {
            Thread t = new Thread(r, CLEANER_THREAD_NAME);
            t.setDaemon(true);
            return t;
        };
    }
}