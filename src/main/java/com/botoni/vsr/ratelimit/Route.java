package com.botoni.vsr.ratelimit;

import com.botoni.vsr.exception.custom.RouteException;
import com.botoni.vsr.exception.enums.problem.RouteProblem;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

record Route(RequestMatcher matcher) implements RequestMatcher {

    private static final String VERSIONED_API = "/api/*";
    private static final String SEPARATOR = "/";

    Route {
        if (matcher == null) {
            throw new RouteException(RouteProblem.MISSING_MATCHER);
        }
    }

    static Route of(HttpMethod method, String path) {
        return new Route(versioned(method, path));
    }

    static Route any() {
        return new Route(AnyRequestMatcher.INSTANCE);
    }

    @Override
    public boolean matches(@NonNull HttpServletRequest request) {
        return matcher.matches(request);
    }

    private static RequestMatcher versioned(HttpMethod method, String path) {
        if (method == null) {
            throw new RouteException(RouteProblem.MISSING_METHOD);
        }
        if (!isAbsolute(path)) {
            throw new RouteException(RouteProblem.INVALID_PATH, path);
        }
        return PathPatternRequestMatcher.pathPattern(method, VERSIONED_API + path);
    }

    private static boolean isAbsolute(String path) {
        return path != null && path.startsWith(SEPARATOR);
    }
}
