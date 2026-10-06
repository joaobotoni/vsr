package com.botoni.vsr.filter;

import com.botoni.vsr.exception.custom.RateLimitException;
import com.botoni.vsr.exception.enums.problem.RateLimitProblem;
import com.botoni.vsr.lib.TokenBucket.RateLimitResult;
import com.botoni.vsr.ratelimit.Limit;
import com.botoni.vsr.ratelimit.RateLimit;
import com.botoni.vsr.ratelimit.RateLimitPolicy;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String LIMIT = "X-RateLimit-Limit";
    private static final String REMAINING = "X-RateLimit-Remaining";
    private static final String RESET = "X-RateLimit-Reset";
    private static final String KEY = "ip:%s";
    private static final long MIN_RETRY = 1;

    private final RateLimitPolicy policy;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        RateLimit rateLimit = policy.resolve(request);
        RateLimitResult result = consume(rateLimit, request);
        long retry = retry(result);

        headers(response, rateLimit.limit(), result, retry);

        if (isExceeded(result)) {
            retryAfter(response, retry);
            throw new RateLimitException(RateLimitProblem.EXCEEDED, retry);
        }
        chain.doFilter(request, response);
    }

    private static RateLimitResult consume(RateLimit rateLimit, HttpServletRequest request) {
        return rateLimit.consume(key(request));
    }

    private static void headers(HttpServletResponse response, Limit limit, RateLimitResult result, long retry) {
        response.setHeader(LIMIT, String.valueOf(limit.capacity()));
        response.setHeader(REMAINING, String.valueOf(remaining(result)));
        response.setHeader(RESET, String.valueOf(reset(retry)));
    }

    private static void retryAfter(HttpServletResponse response, long retry) {
        response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(retry));
    }

    private static boolean isExceeded(RateLimitResult result) {
        return !result.allowed();
    }

    private static String key(HttpServletRequest request) {
        return String.format(KEY, request.getRemoteAddr());
    }

    private static long remaining(RateLimitResult result) {
        return (long) result.remaining();
    }

    private static long reset(long retry) {
        return Instant.now().getEpochSecond() + retry;
    }

    private static long retry(RateLimitResult result) {
        return Math.max(MIN_RETRY, (long) Math.ceil(result.resetIn()));
    }
}