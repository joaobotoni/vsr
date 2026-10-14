package com.botoni.vsr.ratelimit;

import com.botoni.vsr.properties.RateLimitProperties;
import org.springframework.http.HttpMethod;

import java.util.List;

final class Rules {

    private Rules() {
    }

    static List<Rule> from(RateLimitProperties properties) {
        return List.of(
                rule(HttpMethod.POST, "/auth/login", properties.login()),
                rule(HttpMethod.POST, "/auth/register", properties.register()),
                rule(HttpMethod.POST, "/auth/refresh", properties.refresh()),
                rule(HttpMethod.PATCH, "/users/me/password", properties.password()),
                rule(HttpMethod.POST, "/evidencias", properties.upload())
        );
    }

    private static Rule rule(HttpMethod method, String path, Limit limit) {
        return Rule.of(Matcher.from(new Route(method, path)), limit);
    }
}
