package com.botoni.vsr.configuration;

import com.botoni.vsr.properties.RateLimitProperties;
import com.botoni.vsr.ratelimit.RateLimit;
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
    public RateLimit accountRateLimit(RateLimitProperties properties) {
        return RateLimit.forAccount(properties.account());
    }
}
