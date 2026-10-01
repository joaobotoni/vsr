package com.botoni.vsr.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

record Route(RequestMatcher matcher) {

    private static final PathPatternRequestMatcher.Builder PATHS = PathPatternRequestMatcher.withDefaults();
    private static final String PATTERN = "/api/*%s";

    static Route any() {
        return new Route(AnyRequestMatcher.INSTANCE);
    }

    static Route of(HttpMethod method, String path) {
        return new Route(PATHS.matcher(method, String.format(PATTERN, path)));
    }

    boolean matches(HttpServletRequest request) {
        return matcher.matches(request);
    }
}