package com.botoni.vsr.ratelimit;

import com.botoni.vsr.properties.RateLimitProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;

import java.util.List;

public final class RateLimitPolicy implements AutoCloseable {

    private final List<RateLimit> rules;

    private final RateLimit fallback;

    RateLimitPolicy(List<RateLimit> rules, RateLimit fallback) {
        this.rules = List.copyOf(rules);
        this.fallback = fallback;
    }

    public static RateLimitPolicy from(RateLimitProperties properties) {
        return new RateLimitPolicy(rules(properties), RateLimit.fallback(properties.api()));
    }

    private static List<RateLimit> rules(RateLimitProperties properties) {
        return List.of(
                RateLimit.forRoute(Route.of(HttpMethod.POST, "/auth/login"), properties.login()),
                RateLimit.forRoute(Route.of(HttpMethod.POST, "/auth/register"), properties.register()),
                RateLimit.forRoute(Route.of(HttpMethod.POST, "/auth/refresh"), properties.refresh()),
                RateLimit.forRoute(Route.of(HttpMethod.PATCH, "/users/me/password"), properties.password()),
                RateLimit.forRoute(Route.of(HttpMethod.POST, "/evidencias"), properties.upload())
        );
    }

    public RateLimit resolve(HttpServletRequest request) {
        for (RateLimit rule : rules) {
            if (rule.matches(request)) {
                return rule;
            }
        }
        return fallback;
    }

    @Override
    public void close() {
        for (RateLimit rule : rules) {
            rule.close();
        }
        fallback.close();
    }
}
