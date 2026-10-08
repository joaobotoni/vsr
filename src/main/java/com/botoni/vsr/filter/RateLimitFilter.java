package com.botoni.vsr.filter;

import com.botoni.vsr.exception.custom.RateLimitException;
import com.botoni.vsr.exception.enums.problem.RateLimitProblem;
import com.botoni.vsr.lib.ClientNetwork;
import com.botoni.vsr.ratelimit.Limit;
import com.botoni.vsr.ratelimit.Quota;
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

    private final RateLimitPolicy policy;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        RateLimit rateLimit = policy.resolve(request);
        Quota quota = consume(rateLimit, request);
        headers(response, rateLimit.limit(), quota);
        retryAfterIfExceeded(response, quota);
        proceed(request, response, chain, quota);
    }

    private static Quota consume(RateLimit rateLimit, HttpServletRequest request) {
        return rateLimit.consume(key(request));
    }

    private static void headers(HttpServletResponse response, Limit limit, Quota quota) {
        response.setHeader(LIMIT, String.valueOf(limit.capacity()));
        response.setHeader(REMAINING, String.valueOf(remaining(quota)));
        response.setHeader(RESET, String.valueOf(reset(quota.retryAfter())));
    }

    private static void retryAfterIfExceeded(HttpServletResponse response, Quota quota) {
        if (!quota.exceeded()) {
            return;
        }
        response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(quota.retryAfter()));
    }

    private static void proceed(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                Quota quota) throws ServletException, IOException {
        if (quota.exceeded()) {
            throw new RateLimitException(RateLimitProblem.EXCEEDED, quota.retryAfter());
        }
        chain.doFilter(request, response);
    }

    private static String key(HttpServletRequest request) {
        return String.format(KEY, ClientNetwork.of(request.getRemoteAddr()));
    }

    private static long remaining(Quota quota) {
        return (long) quota.remaining();
    }

    private static long reset(long retry) {
        return Instant.now().getEpochSecond() + retry;
    }
}