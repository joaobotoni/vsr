package com.botoni.vsr.ratelimit;

import com.botoni.vsr.properties.RateLimitProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.Map;

public final class RateLimitPolicy implements AutoCloseable {

    private final Map<RequestMatcher, TokenBucket> routes;
    private final TokenBucket fallback;
    private final TokenBucket account;
    private final TokenBucket ceiling;

    private RateLimitPolicy(Map<RequestMatcher, TokenBucket> routes, TokenBucket fallback, TokenBucket account, TokenBucket ceiling) {
        this.routes = routes;
        this.fallback = fallback;
        this.account = account;
        this.ceiling = ceiling;
    }

    public static RateLimitPolicy from(RateLimitProperties properties) {
        return new RateLimitPolicy(Routes.from(properties), new TokenBucket(properties.api()),
                new TokenBucket(properties.account()), new TokenBucket(properties.ceiling()));
    }

    public TokenBucket resolve(HttpServletRequest request) {
        for (Map.Entry<RequestMatcher, TokenBucket> route : routes.entrySet()) {
            if (route.getKey().matches(request)) {
                return route.getValue();
            }
        }
        return fallback;
    }

    public TokenBucket account() {
        return account;
    }

    public TokenBucket ceiling() {
        return ceiling;
    }

    @Override
    public void close() {
        for (TokenBucket tokenBucket : routes.values()) {
            tokenBucket.close();
        }
        fallback.close();
        account.close();
        ceiling.close();
    }
}
