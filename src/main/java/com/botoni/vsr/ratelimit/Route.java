package com.botoni.vsr.ratelimit;

import com.botoni.vsr.exception.custom.RouteException;
import com.botoni.vsr.exception.enums.problem.RouteProblem;
import org.springframework.http.HttpMethod;

record Route(HttpMethod method, String path) {
    private static final String VERSIONED_API = "/api/*";
    private static final String SEPARATOR = "/";

    Route {
        if (method == null) {
            throw new RouteException(RouteProblem.MISSING_METHOD);
        }
        validate(path);
    }

    static Route of(HttpMethod method, String path) {
        return new Route(method, path);
    }

    String pattern() {
        return VERSIONED_API + path;
    }

    private static void validate(String path) {
        if (isRelative(path)) {
            throw new RouteException(RouteProblem.INVALID_PATH, path);
        }
    }

    private static boolean isRelative(String path) {
        return path == null || !path.startsWith(SEPARATOR);
    }
}
