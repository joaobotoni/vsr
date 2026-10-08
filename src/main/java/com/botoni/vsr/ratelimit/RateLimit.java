package com.botoni.vsr.ratelimit;

import jakarta.servlet.http.HttpServletRequest;

public final class RateLimit implements AutoCloseable {

    private final Route route;
    private final Limit limit;
    private final TokenBucket bucket;

    private RateLimit(Route route, Limit limit) {
        this.route = route;
        this.limit = limit;
        this.bucket = new TokenBucket(limit);
    }

    static RateLimit of(Route route, Limit limit) {
        return new RateLimit(route, limit);
    }

    public static RateLimit any(Limit limit) {
        return new RateLimit(Route.any(), limit);
    }

    public Quota consume(String key) {
        return bucket.consume(key);
    }

    public Quota check(String key) {
        return bucket.check(key);
    }

    public Limit limit() {
        return limit;
    }

    boolean matches(HttpServletRequest request) {
        return route.matches(request);
    }

    @Override
    public void close() {
        bucket.close();
    }
}
