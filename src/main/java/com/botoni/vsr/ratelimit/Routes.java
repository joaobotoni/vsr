package com.botoni.vsr.ratelimit;

import com.botoni.vsr.properties.RateLimitProperties;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.LinkedHashMap;
import java.util.Map;

final class Routes {

    private Routes() {
    }

    static Map<RequestMatcher, TokenBucket> from(RateLimitProperties properties) {
        Map<RequestMatcher, TokenBucket> routes = new LinkedHashMap<>();
        routes.put(matcher(Route.of(HttpMethod.POST, "/auth/login")), new TokenBucket(properties.login()));
        routes.put(matcher(Route.of(HttpMethod.POST, "/auth/register")), new TokenBucket(properties.register()));
        routes.put(matcher(Route.of(HttpMethod.POST, "/auth/refresh")), new TokenBucket(properties.refresh()));
        routes.put(matcher(Route.of(HttpMethod.PATCH, "/users/me/password")), new TokenBucket(properties.password()));
        return routes;
    }

    private static RequestMatcher matcher(Route route) {
        return PathPatternRequestMatcher.pathPattern(route.method(), route.pattern());
    }
}
