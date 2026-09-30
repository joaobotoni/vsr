package com.botoni.vsr.configuration;

import com.botoni.vsr.configuration.properties.RateLimitProperties;
import com.botoni.vsr.lib.TokenBucket;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitConfig {

    @Bean
    public TokenBucket tokenBucket(RateLimitProperties props) {
        return new TokenBucket(props.capacity(), props.refillRate(), props.refillIntervalSeconds());
    }
}
