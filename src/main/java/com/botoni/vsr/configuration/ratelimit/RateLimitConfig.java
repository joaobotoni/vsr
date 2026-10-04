package com.botoni.vsr.configuration.ratelimit;

import com.botoni.vsr.properties.ratelimit.RateLimitProperties;
import com.botoni.vsr.ratelimit.RateLimitPolicy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitConfig {

    @Bean
    public RateLimitPolicy rateLimitPolicy(RateLimitProperties props) {
        return RateLimitPolicy.from(props);
    }
}
