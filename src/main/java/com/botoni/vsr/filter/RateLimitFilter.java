package com.botoni.vsr.filter;

import com.botoni.vsr.exception.custom.RateLimitException;
import com.botoni.vsr.exception.enums.problem.RateLimitProblem;
import com.botoni.vsr.network.ClientNetwork;
import com.botoni.vsr.ratelimit.Limit;
import com.botoni.vsr.ratelimit.Allowance;
import com.botoni.vsr.ratelimit.Rule;
import com.botoni.vsr.ratelimit.RateLimitPolicy;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Clock;
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
    private final Clock clock;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        Rule rule = policy.resolve(request);
        Allowance allowance = consume(rule, request);
        headers(response, rule.limit(), allowance);
        advise(response, allowance);
        proceed(request, response, chain, allowance);
    }

    private static Allowance consume(Rule rule, HttpServletRequest request) {
        return rule.consume(key(request));
    }

    private void headers(HttpServletResponse response, Limit limit, Allowance allowance) {
        response.setHeader(LIMIT, String.valueOf(limit.capacity()));
        response.setHeader(REMAINING, String.valueOf(remaining(allowance)));
        response.setHeader(RESET, String.valueOf(deadline(allowance.retry())));
    }

    private static void advise(HttpServletResponse response, Allowance allowance) {
        if (!allowance.exceeded()) {
            return;
        }
        response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(allowance.retry()));
    }

    private static void proceed(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Allowance allowance)
            throws ServletException, IOException {
        if (allowance.exceeded()) {
            throw new RateLimitException(RateLimitProblem.EXCEEDED, allowance.retry());
        }
        chain.doFilter(request, response);
    }

    private static String key(HttpServletRequest request) {
        return String.format(KEY, ClientNetwork.key(request.getRemoteAddr()));
    }

    private static long remaining(Allowance allowance) {
        return (long) allowance.remaining();
    }

    private long deadline(long retry) {
        return Instant.now(clock).getEpochSecond() + retry;
    }
}