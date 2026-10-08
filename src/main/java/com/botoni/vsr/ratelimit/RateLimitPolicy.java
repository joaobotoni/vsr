package com.botoni.vsr.ratelimit;

import com.botoni.vsr.properties.RateLimitProperties;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public final class RateLimitPolicy implements AutoCloseable {

    private final List<RateLimit> rules;
    private final RateLimit fallback;

    private RateLimitPolicy(List<RateLimit> rules, RateLimit fallback) {
        this.rules = List.copyOf(rules);
        this.fallback = fallback;
    }

    public static RateLimitPolicy from(RateLimitProperties properties) {
        return new RateLimitPolicy(Rules.from(properties), RateLimit.any(properties.api()));
    }

    public RateLimit resolve(HttpServletRequest request) {
        return rules.stream()
                .filter(rule -> rule.matches(request))
                .findFirst()
                .orElse(fallback);
    }

    @Override
    public void close() {
        rules.forEach(RateLimit::close);
        fallback.close();
    }
}
