package com.botoni.vsr.ratelimit;

import com.botoni.vsr.exception.custom.MatcherException;
import com.botoni.vsr.exception.enums.problem.MatcherProblem;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NonNull;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

record Matcher(RequestMatcher delegate) implements RequestMatcher {

    Matcher {
        if (missing(delegate)) {
            throw new MatcherException(MatcherProblem.MISSING_DELEGATE);
        }
    }

    static Matcher from(Route route) {
        return new Matcher(pattern(route));
    }

    static Matcher any() {
        return new Matcher(AnyRequestMatcher.INSTANCE);
    }

    @Override
    public boolean matches(@NonNull HttpServletRequest request) {
        return delegate.matches(request);
    }

    private static RequestMatcher pattern(Route route) {
        return PathPatternRequestMatcher.pathPattern(route.method(), route.pattern());
    }

    private static boolean missing(RequestMatcher delegate) {
        return delegate == null;
    }
}
