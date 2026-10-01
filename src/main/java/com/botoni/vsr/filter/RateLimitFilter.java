package com.botoni.vsr.filter;

import com.botoni.vsr.configuration.properties.RateLimitProperties;
import com.botoni.vsr.exception.infrastructure.RateLimitException;
import com.botoni.vsr.lib.TokenBucket;
import com.botoni.vsr.lib.TokenBucket.RateLimitResult;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final TokenBucket limiter;
    private final RateLimitProperties props;

    public RateLimitFilter(TokenBucket limiter, RateLimitProperties props) {
        this.limiter = limiter;
        this.props = props;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        RateLimitResult result = limiter.allow(key(request));
        long refill = refillSeconds();
        writeRateLimitHeaders(response, result, refill);

        if (!result.allowed()) {
            response.setHeader(HttpHeaders.RETRY_AFTER, String.format("%d", refill));
            throw new RateLimitException.Exceeded(refill);
        }

        chain.doFilter(request, response);
    }

    private void writeRateLimitHeaders(HttpServletResponse response, RateLimitResult result, long refill) {
        response.setHeader("X-RateLimit-Limit", String.format("%d", props.capacity()));
        response.setHeader("X-RateLimit-Remaining", String.format("%d", (int) result.remaining()));
        response.setHeader("X-RateLimit-Reset", String.format("%d", Instant.now().getEpochSecond() + refill));
    }

    private long refillSeconds() {
        return (long) Math.ceil(props.refillIntervalSeconds());
    }

    private static String key(HttpServletRequest request) {
        return String.format("ip:%s", request.getRemoteAddr());
    }
}
