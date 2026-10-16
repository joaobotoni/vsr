package com.botoni.vsr.ratelimit;

import com.botoni.vsr.exception.custom.LimitException;
import com.botoni.vsr.exception.enums.problem.LimitProblem;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class TokenBucket implements AutoCloseable {

    public record Limit(int capacity, double rate, Duration period) {}

    public record Allowance(boolean allowed, double remaining, double reset) {}

    record Bucket(double tokens, double lastRefill) {}

    private static final long MIN_RETRY = 1;
    private static final long MIN_PERIOD = 1000L;
    private static final int MAX_KEYS = 200_000;
    private static final double RELIEF_INTERVAL = 1;
    private static final String CLEANER_THREAD_NAME = "token-bucket-cleaner";
    private static final ScheduledThreadPoolExecutor CLEANER = cleaner();

    private final Limit limit;
    private final double ttl;
    private final int maxKeys;
    private final double refillInterval;
    private final ConcurrentMap<String, Bucket> buckets;
    private volatile ScheduledFuture<?> cleaning;
    private double lastRelief = Double.NEGATIVE_INFINITY;

    public TokenBucket(Limit limit) {
        this(limit, MAX_KEYS, new ConcurrentHashMap<>());
        schedule();
    }

    TokenBucket(Limit limit, int maxKeys, ConcurrentMap<String, Bucket> buckets) {
        validate(limit);
        this.limit = limit;
        this.refillInterval = limit.period().toMillis() / 1000.0;
        this.ttl = Math.ceil(limit.capacity() / limit.rate()) * refillInterval;
        this.maxKeys = maxKeys;
        this.buckets = buckets;
    }

    public Allowance allow(String key) {
        return allow(key, now());
    }

    public Allowance allow(String key, double now) {
        if (crowded(key, now)) {
            return new Allowance(false, 0, refillInterval);
        }

        Allowance[] result = new Allowance[1];

        buckets.compute(key, (k, bucket) -> {
            double tokens = bucket == null ? limit.capacity() : bucket.tokens();
            double lastRefill = bucket == null ? now : bucket.lastRefill();

            double timePassed = now - lastRefill;
            double refills = Math.floor(timePassed / refillInterval);

            if (refills > 0) {
                tokens = Math.min(limit.capacity(), tokens + (refills * limit.rate()));
                lastRefill = lastRefill + (refills * refillInterval);
            }

            boolean allowed = false;
            if (tokens >= 1) {
                tokens = tokens - 1;
                allowed = true;
            }

            result[0] = new Allowance(allowed, Math.floor(tokens), refillInterval - (now - lastRefill));

            return new Bucket(tokens, lastRefill);
        });

        return result[0];
    }

    public Limit limit() {
        return limit;
    }

    public static long retry(Allowance allowance) {
        return Math.max(MIN_RETRY, (long) Math.ceil(allowance.reset()));
    }

    void evict(double now) {
        removeKeys(findIdleKeys(now), now);
    }

    @Override
    public void close() {
        if (cleaning != null) {
            cleaning.cancel(false);
        }
        buckets.clear();
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

    private void removeKeys(List<String> keys, double now) {
        for (String key : keys) {
            buckets.computeIfPresent(key, (k, bucket) -> idle(bucket, now) ? null : bucket);
        }
    }

    private boolean crowded(String key, double now) {
        if (buckets.size() < maxKeys || buckets.containsKey(key)) {
            return false;
        }
        if (due(now)) {
            evict(now);
        }
        return buckets.size() >= maxKeys;
    }

    private synchronized boolean due(double now) {
        if (now - lastRelief < RELIEF_INTERVAL) {
            return false;
        }
        lastRelief = now;
        return true;
    }

    private boolean idle(Bucket bucket, double now) {
        return now - bucket.lastRefill() >= ttl;
    }

    private static void validate(Limit limit) {
        if (limit.capacity() <= 0) {
            throw new LimitException(LimitProblem.INVALID_CAPACITY);
        }
        if (limit.rate() <= 0) {
            throw new LimitException(LimitProblem.INVALID_REFILL_RATE);
        }
        if (limit.period() == null || limit.period().isZero() || limit.period().isNegative()) {
            throw new LimitException(LimitProblem.INVALID_REFILL_INTERVAL);
        }
    }

    private long period() {
        return Math.max(MIN_PERIOD, (long) Math.ceil(ttl * 1000));
    }

    void schedule() {
        cleaning = CLEANER.scheduleAtFixedRate(() -> evict(now()), period(), period(), TimeUnit.MILLISECONDS);
    }

    private static double now() {
        return System.nanoTime() / 1_000_000_000.0;
    }

    private static ScheduledThreadPoolExecutor cleaner() {
        ScheduledThreadPoolExecutor cleaner = new ScheduledThreadPoolExecutor(1, daemon());
        cleaner.setRemoveOnCancelPolicy(true);
        return cleaner;
    }

    private static ThreadFactory daemon() {
        return r -> {
            Thread thread = new Thread(r, CLEANER_THREAD_NAME);
            thread.setDaemon(true);
            return thread;
        };
    }
}
