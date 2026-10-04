package com.botoni.vsr.infra.configuration;

import com.botoni.vsr.infra.configuration.properties.RateLimitProperties;
import com.botoni.vsr.infra.ratelimit.RateLimitPolicy;
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
