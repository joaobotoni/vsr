package com.botoni.vsr.configuration;

import com.botoni.vsr.security.OpaqueToken;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpaqueTokenConfig {

    @Bean
    public OpaqueToken opaqueToken() {
        return new OpaqueToken();
    }
}
