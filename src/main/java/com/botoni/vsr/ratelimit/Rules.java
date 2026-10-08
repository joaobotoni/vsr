package com.botoni.vsr.ratelimit;

import com.botoni.vsr.properties.RateLimitProperties;
import org.springframework.http.HttpMethod;

import java.util.List;

final class Rules {

    private Rules() {
    }

    static List<RateLimit> from(RateLimitProperties properties) {
        return List.of(
                RateLimit.of(Route.of(HttpMethod.POST, "/auth/login"), properties.login()),
                RateLimit.of(Route.of(HttpMethod.POST, "/auth/register"), properties.register()),
                RateLimit.of(Route.of(HttpMethod.POST, "/auth/refresh"), properties.refresh()),
                RateLimit.of(Route.of(HttpMethod.PATCH, "/users/me/password"), properties.password()),
                RateLimit.of(Route.of(HttpMethod.POST, "/evidencias"), properties.upload())
        );
    }
}
