package com.botoni.vsr.lib;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class TokenBucket implements AutoCloseable {

    public record RateLimitResult(boolean allowed, double remaining, double resetIn) {

        private static final long MIN_RETRY = 1;

        public boolean exceeded() {
            return !allowed;
        }

        public long retryAfter() {
            return Math.max(MIN_RETRY, (long) Math.ceil(resetIn));
        }
    }

    private record Bucket(double tokens, double lastRefill, boolean allowed) {}

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
        return result(buckets.compute(key, (k, bucket) -> consume(refill(bucket, now))), now);
    }

    public RateLimitResult peek(String key) {
        return peek(key, now());
    }

    public RateLimitResult peek(String key, double now) {
        return result(buckets.compute(key, (k, bucket) -> inspect(refill(bucket, now))), now);
    }

    private Bucket refill(Bucket bucket, double now) {
        if (bucket == null) {
            return new Bucket(capacity, now, false);
        }
        return replenish(bucket, refills(bucket, now));
    }

    private Bucket replenish(Bucket bucket, double refills) {
        if (refills <= 0) {
            return bucket;
        }
        return new Bucket(Math.min(capacity, bucket.tokens() + refills * refillRate),
                bucket.lastRefill() + refills * refillInterval, false);
    }

    private static Bucket consume(Bucket bucket) {
        if (bucket.tokens() < 1) {
            return new Bucket(bucket.tokens(), bucket.lastRefill(), false);
        }
        return new Bucket(bucket.tokens() - 1, bucket.lastRefill(), true);
    }

    private static Bucket inspect(Bucket bucket) {
        return new Bucket(bucket.tokens(), bucket.lastRefill(), bucket.tokens() >= 1);
    }

    private double refills(Bucket bucket, double now) {
        return Math.floor((now - bucket.lastRefill()) / refillInterval);
    }

    private RateLimitResult result(Bucket bucket, double now) {
        return new RateLimitResult(bucket.allowed(), Math.floor(bucket.tokens()),
                bucket.lastRefill() + refillInterval - now);
    }

    void evict(double now) {
        buckets.entrySet().removeIf(entry -> idle(entry.getValue(), now));
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

    @Override
    public void close() {
        cleaner.shutdownNow();
        buckets.clear();
    }
}
