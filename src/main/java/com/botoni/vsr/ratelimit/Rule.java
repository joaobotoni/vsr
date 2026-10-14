package com.botoni.vsr.ratelimit;

import jakarta.servlet.http.HttpServletRequest;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class Rule implements AutoCloseable {

    private final Matcher matcher;
    private final Limit limit;
    private final TokenBucket tokenBucket;
    private final Sweeper sweeper;

    private Rule(Matcher matcher, Limit limit, ConcurrentMap<String, Bucket> buckets) {
        this.matcher = matcher;
        this.limit = limit;
        this.tokenBucket = new TokenBucket(limit, buckets);
        this.sweeper = new Sweeper(limit, buckets);
    }

    static Rule of(Matcher matcher, Limit limit) {
        return new Rule(matcher, limit, new ConcurrentHashMap<>());
    }

    static Rule any(Limit limit) {
        return of(Matcher.any(), limit);
    }

    public Verdict consume(String key) {
        return tokenBucket.consume(key);
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
}
