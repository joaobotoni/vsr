package com.botoni.vsr.ratelimit;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

class TokenBucket implements AutoCloseable {

    private static final long MIN_PERIOD = 1000L;
    private static final String CLEANER_THREAD_NAME = "token-bucket-cleaner";

    private final int capacity;
    private final double refillRate;
    private final double refillInterval;
    private final double ttl;
    private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleaner = cleaner();

    TokenBucket(Limit limit) {
        this.capacity = limit.capacity();
        this.refillRate = limit.refillRate();
        this.refillInterval = limit.seconds();
        this.ttl = ttl();
        schedule();
    }

    Quota consume(String key) {
        double now = now();
        return quota(buckets.compute(key, (k, bucket) -> take(refill(bucket, now))), now);
    }

    Quota check(String key) {
        double now = now();
        return quota(buckets.compute(key, (k, bucket) -> inspect(refill(bucket, now))), now);
    }

    @Override
    public void close() {
        cleaner.shutdownNow();
        buckets.clear();
    }

    private static double now() {
        return System.nanoTime() / 1_000_000_000.0;
    }

    private Bucket refill(Bucket bucket, double now) {
        if (bucket == null) {
            return new Bucket(capacity, now, false);
        }
        return replenish(bucket, refills(bucket, now));
    }

    private double refills(Bucket bucket, double now) {
        return Math.floor((now - bucket.lastRefill()) / refillInterval);
    }

    private Bucket replenish(Bucket bucket, double refills) {
        if (refills <= 0) {
            return bucket;
        }
        return new Bucket(Math.min(capacity, bucket.tokens() + refills * refillRate),
                bucket.lastRefill() + refills * refillInterval, false);
    }

    private static Bucket take(Bucket bucket) {
        if (bucket.tokens() < 1) {
            return new Bucket(bucket.tokens(), bucket.lastRefill(), false);
        }
        return new Bucket(bucket.tokens() - 1, bucket.lastRefill(), true);
    }

    private static Bucket inspect(Bucket bucket) {
        return new Bucket(bucket.tokens(), bucket.lastRefill(), bucket.tokens() >= 1);
    }

    private Quota quota(Bucket bucket, double now) {
        return new Quota(bucket.allowed(), Math.floor(bucket.tokens()),
                bucket.lastRefill() + refillInterval - now);
    }

    private double ttl() {
        return Math.ceil(capacity / refillRate) * refillInterval;
    }

    private void schedule() {
        cleaner.scheduleAtFixedRate(this::evict, period(), period(), TimeUnit.MILLISECONDS);
    }

    private long period() {
        return Math.max(MIN_PERIOD, (long) Math.ceil(ttl * 1000));
    }

    private void evict() {
        double now = now();
        buckets.entrySet().removeIf(entry -> idle(entry.getValue(), now));
    }

    private boolean idle(Bucket bucket, double now) {
        return now - bucket.lastRefill() >= ttl;
    }

    private static ScheduledExecutorService cleaner() {
        return Executors.newSingleThreadScheduledExecutor(daemon());
    }

    private static ThreadFactory daemon() {
        return runnable -> {
            Thread thread = new Thread(runnable, CLEANER_THREAD_NAME);
            thread.setDaemon(true);
            return thread;
        };
    }
}
