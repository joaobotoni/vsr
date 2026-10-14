package com.botoni.vsr.ratelimit;

import com.botoni.vsr.exception.custom.RouteException;
import com.botoni.vsr.exception.enums.problem.RouteProblem;
import org.springframework.http.HttpMethod;

record Route(HttpMethod method, String path) {

    private static final String VERSIONED_API = "/api/*";
    private static final String SEPARATOR = "/";

    Route {
        if (missing(method)) {
            throw new RouteException(RouteProblem.MISSING_METHOD);
        }
        if (relative(path)) {
            throw new RouteException(RouteProblem.INVALID_PATH, path);
        }
    }

    String pattern() {
        return VERSIONED_API + path;
    }

    private static boolean missing(Object value) {
        return value == null;
    }

    private static boolean relative(String path) {
        return missing(path) || !path.startsWith(SEPARATOR);
    }
}
