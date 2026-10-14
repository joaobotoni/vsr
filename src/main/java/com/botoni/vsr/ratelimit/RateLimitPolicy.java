package com.botoni.vsr.ratelimit;

import com.botoni.vsr.properties.RateLimitProperties;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public final class RateLimitPolicy implements AutoCloseable {

    private final List<Rule> rules;
    private final Rule fallback;

    private RateLimitPolicy(List<Rule> rules, Rule fallback) {
        this.rules = rules;
        this.fallback = fallback;
    }

    public static RateLimitPolicy from(RateLimitProperties properties) {
        return new RateLimitPolicy(Rules.from(properties), Rule.any(properties.api()));
    }

    public Rule resolve(HttpServletRequest request) {
        for (Rule rule : rules) {
            if (rule.matches(request)) {
                return rule;
            }
        }
        return fallback;
    }

    @Override
    public void close() {
        for (Rule rule : rules) {
            rule.close();
        }
        fallback.close();
    }
}
