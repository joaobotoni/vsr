package com.botoni.vsr.filter;

import com.botoni.vsr.exception.infrastructure.RateLimitException;
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
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String LIMIT = "X-RateLimit-Limit";
    private static final String REMAINING = "X-RateLimit-Remaining";
    private static final String RESET = "X-RateLimit-Reset";
    private static final String KEY = "ip:%s";
    private static final long MIN_RETRY = 1;

    private final RateLimitPolicy policy;

    public RateLimitFilter(RateLimitPolicy policy) {
        this.policy = policy;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {

        RateLimit rateLimit = policy.resolve(request);
        Limit limit = rateLimit.limit();
        RateLimitResult result = rateLimit.consume(key(request));

        long retry = retryAfter(limit);
        headers(response, limit, result, retry);

        if (!result.allowed()) {
            response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(retry));
            throw new RateLimitException.Exceeded(retry);
        }

        chain.doFilter(request, response);
    }

    private static String key(HttpServletRequest request) {
        return String.format(KEY, request.getRemoteAddr());
    }

    private static void headers(HttpServletResponse response, Limit limit, RateLimitResult result, long retry) {
        long reset = Instant.now().getEpochSecond() + retry;
        response.setHeader(LIMIT, String.valueOf(limit.capacity()));
        response.setHeader(REMAINING, String.valueOf((long) result.remaining()));
        response.setHeader(RESET, String.valueOf(reset));
    }

    private static long retryAfter(Limit limit) {
        long millis = limit.refillInterval().toMillis();
        long seconds = (millis + 999) / 1000;
        return Math.max(MIN_RETRY, seconds);
    }
}