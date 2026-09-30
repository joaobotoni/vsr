package com.botoni.vsr.filter;

import com.botoni.vsr.configuration.properties.RateLimitProperties;
import com.botoni.vsr.lib.TokenBucket;
import com.botoni.vsr.lib.TokenBucket.RateLimitResult;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

        RateLimitResult result = limiter.allow(String.format("ip:%s", request.getRemoteAddr()));
        long refill = (long) Math.ceil(props.refillIntervalSeconds());

        response.setHeader("X-RateLimit-Limit", String.format("%d", props.capacity()));
        response.setHeader("X-RateLimit-Remaining", String.format("%d", (int) result.remaining()));
        response.setHeader("X-RateLimit-Reset", String.format("%d", Instant.now().getEpochSecond() + refill));

        if (result.allowed()) {
            chain.doFilter(request, response);
            return;
        }

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", String.format("%d", refill));
        response.getWriter().write("{\"error\": \"Rate limit exceeded\"}");
    }
}