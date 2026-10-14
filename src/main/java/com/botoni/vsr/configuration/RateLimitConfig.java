package com.botoni.vsr.configuration;

import com.botoni.vsr.attempt.IAttempt;
import com.botoni.vsr.properties.RateLimitProperties;
import com.botoni.vsr.ratelimit.AttemptLimiter;
import com.botoni.vsr.ratelimit.RateLimitPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimitConfig {

    @Bean
    public RateLimitPolicy rateLimitPolicy(RateLimitProperties properties) {
        return RateLimitPolicy.from(properties);
    }

    @Bean
    public IAttempt attemptLimiter(RateLimitProperties properties) {
        return AttemptLimiter.from(properties.account());
    }
}
