package com.botoni.vsr.ratelimit;

import jakarta.servlet.http.HttpServletRequest;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class Rule implements AutoCloseable {

    private static final int MAX_KEYS = 50_000;
    private static final double NOTHING = 0;

    private final int max;
    private final Limit limit;
    private final Matcher matcher;
    private final ConcurrentMap<String, Bucket> buckets;
    private final TokenBucket tokenBucket;
    private final Sweeper sweeper;

    private Rule(Matcher matcher, Limit limit, int max, ConcurrentMap<String, Bucket> buckets) {
        this.matcher = matcher;
        this.limit = limit;
        this.max = max;
        this.buckets = buckets;
        this.tokenBucket = new TokenBucket(limit, buckets);
        this.sweeper = new Sweeper(limit, buckets);
    }

    static Rule of(Matcher matcher, Limit limit) {
        return sized(matcher, limit, MAX_KEYS);
    }

    static Rule sized(Matcher matcher, Limit limit, int max) {
        return new Rule(matcher, limit, max, new ConcurrentHashMap<>());
    }

    static Rule any(Limit limit) {
        return of(Matcher.any(), limit);
    }

    public Allowance consume(String key) {
        vacate(key);
        return crowded(key) ? refused() : tokenBucket.consume(key);
    }

    public Limit limit() {
        return limit;
    }

    void refund(String key) {
        tokenBucket.refund(key);
    }

    boolean matches(HttpServletRequest request) {
        return matcher.matches(request);
    }

    @Override
    public void close() {
        sweeper.close();
    }

    private Allowance refused() {
        return new Allowance(false, NOTHING, limit.interval());
    }

    private void vacate(String key) {
        if (!crowded(key)) {
            return;
        }
        sweeper.relieve(TokenBucket.now());
    }

    private boolean crowded(String key) {
        return buckets.size() >= max && !buckets.containsKey(key);
    }
}
