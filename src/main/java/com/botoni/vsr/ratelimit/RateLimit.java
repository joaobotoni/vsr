package com.botoni.vsr.ratelimit;

import com.botoni.vsr.lib.TokenBucket;
import com.botoni.vsr.lib.TokenBucket.RateLimitResult;
import jakarta.servlet.http.HttpServletRequest;

public final class RateLimit implements AutoCloseable {

    private final Route route;
    private final Limit limit;
    private final TokenBucket bucket;

    private RateLimit(Route route, Limit limit) {
        this.route = route;
        this.limit = limit;
        this.bucket = new TokenBucket(limit.capacity(), limit.refillRate(), limit.refillInterval());
    }

    static RateLimit forRoute(Route route, Limit limit) {
        return new RateLimit(route, limit);
    }

    static RateLimit fallback(Limit limit) {
        return new RateLimit(Route.any(), limit);
    }

    boolean matches(HttpServletRequest request) {
        return route.matches(request);
    }

    public RateLimitResult consume(String key) {
        return bucket.allow(key);
    }

    public Limit limit() {
        return limit;
    }

    @Override
    public void close() {
        bucket.close();
    }
}