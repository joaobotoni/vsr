package com.botoni.vsr.filter;

import com.botoni.vsr.exception.custom.RateLimitException;
import com.botoni.vsr.exception.enums.problem.RateLimitProblem;
import com.botoni.vsr.lib.ClientNetwork;
import com.botoni.vsr.ratelimit.Limit;
import com.botoni.vsr.ratelimit.Verdict;
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
        Verdict verdict = consume(rule, request);
        headers(response, rule.limit(), verdict);
        advise(response, verdict);
        proceed(request, response, chain, verdict);
    }

    private static Verdict consume(Rule rule, HttpServletRequest request) {
        return rule.consume(key(request));
    }

    private void headers(HttpServletResponse response, Limit limit, Verdict verdict) {
        response.setHeader(LIMIT, String.valueOf(limit.capacity()));
        response.setHeader(REMAINING, String.valueOf(remaining(verdict)));
        response.setHeader(RESET, String.valueOf(deadline(verdict.retry())));
    }

    private static void advise(HttpServletResponse response, Verdict verdict) {
        if (!verdict.exceeded()) {
            return;
        }
        response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(verdict.retry()));
    }

    private static void proceed(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Verdict verdict)
            throws ServletException, IOException {
        if (verdict.exceeded()) {
            throw new RateLimitException(RateLimitProblem.EXCEEDED, verdict.retry());
        }
        chain.doFilter(request, response);
    }

    private static String key(HttpServletRequest request) {
        return String.format(KEY, ClientNetwork.of(request.getRemoteAddr()));
    }

    private static long remaining(Verdict verdict) {
        return (long) verdict.remaining();
    }

    private long deadline(long retry) {
        return Instant.now(clock).getEpochSecond() + retry;
    }
}