package com.botoni.vsr.configuration;

import com.botoni.vsr.properties.RefreshTokenProperties;
import com.botoni.vsr.security.OpaqueToken;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpaqueTokenConfig {

    @Bean
    public OpaqueToken opaqueToken(RefreshTokenProperties properties) {
        return new OpaqueToken(properties.secretKey());
    }
}
