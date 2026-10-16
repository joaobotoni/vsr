package com.botoni.vsr.support;

import com.botoni.vsr.properties.RateLimitProperties;
import com.botoni.vsr.ratelimit.RateLimitPolicy;
import com.botoni.vsr.ratelimit.TokenBucket.Limit;

import java.time.Duration;

public final class Policies {

    private static final Limit ROOMY = new Limit(1_000, 1_000, Duration.ofSeconds(1));

    private Policies() {
    }

    public static RateLimitPolicy of(Limit account, Limit ceiling) {
        return RateLimitPolicy.from(new RateLimitProperties(ROOMY, ROOMY, ROOMY, ROOMY, account, ceiling, ROOMY));
    }

    public static RateLimitPolicy account(Limit account) {
        return of(account, ROOMY);
    }
}
