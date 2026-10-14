package com.botoni.vsr.ratelimit;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

final class Sweeper implements AutoCloseable {

    private static final long MIN_PERIOD_SECONDS = 1;
    private static final String THREAD_NAME = "token-bucket-sweeper";
    private static final ScheduledThreadPoolExecutor SCHEDULER = scheduler();

    private final Limit limit;
    private final ConcurrentMap<String, Bucket> buckets;
    private final ScheduledFuture<?> task;

    Sweeper(Limit limit, ConcurrentMap<String, Bucket> buckets) {
        this.limit = limit;
        this.buckets = buckets;
        this.task = schedule();
    }

    void evict(double now) {
        buckets.values().removeIf(bucket -> idle(bucket, now));
    }

    @Override
    public void close() {
        task.cancel(false);
        buckets.clear();
    }

    private ScheduledFuture<?> schedule() {
        return repeat(period());
    }

    private ScheduledFuture<?> repeat(long period) {
        return SCHEDULER.scheduleAtFixedRate(this::sweep, period, period, TimeUnit.SECONDS);
    }

    private void sweep() {
        evict(TokenBucket.now());
    }

    private long period() {
        return Math.max(MIN_PERIOD_SECONDS, ttl());
    }

    private long ttl() {
        return (long) Math.ceil(limit.ttl());
    }

    private boolean idle(Bucket bucket, double now) {
        return bucket.elapsed(now) >= limit.ttl();
    }

    private static ScheduledThreadPoolExecutor scheduler() {
        ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1, daemon());
        scheduler.setRemoveOnCancelPolicy(true);
        return scheduler;
    }

    private static ThreadFactory daemon() {
        return runnable -> {
            Thread thread = new Thread(runnable, THREAD_NAME);
            thread.setDaemon(true);
            return thread;
        };
    }
}