package com.botoni.vsr.shared.lib;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
            throw new IllegalArgumentException("capacity, refillRate and refillInterval must be greater than zero");
        }

        this.capacity = capacity;
        this.refillRate = refillRate;
        this.refillInterval = refillInterval;
        this.ttl = ttl();
        schedule();
    }

    public TokenBucket(int capacity, double refillRate, Duration refillInterval) {
        this(capacity, refillRate, refillInterval.toMillis() / 1000.0);
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
        removeKeys(findIdleKeys(now));
    }

    private List<String> findIdleKeys(double now) {
        List<String> idleKeys = new ArrayList<>();
        for (Map.Entry<String, Bucket> entry : buckets.entrySet()) {
            if (idle(entry.getValue(), now)) {
                idleKeys.add(entry.getKey());
            }
        }
        return idleKeys;
    }

    private void removeKeys(List<String> keys) {
        for (String key : keys) {
            buckets.remove(key);
        }
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
        return Math.max(MIN_PERIOD, (long) Math.ceil(ttl * 1000));
    }

    private boolean idle(Bucket bucket, double now) {
        return now - bucket.lastRefill() >= ttl;
    }

    private void schedule() {
        cleaner.scheduleAtFixedRate(() -> evict(now()), period(), period(), TimeUnit.MILLISECONDS);
    }

    private static double now() {
        return System.nanoTime() / 1_000_000_000.0;
    }

    private static ScheduledExecutorService cleaner() {
        return Executors.newSingleThreadScheduledExecutor(daemon());
    }

    private static ThreadFactory daemon() {
        return r -> {
            Thread thread = new Thread(r, CLEANER_THREAD_NAME);
            thread.setDaemon(true);
            return thread;
        };
    }
}