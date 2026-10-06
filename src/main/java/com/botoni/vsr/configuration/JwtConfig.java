package com.botoni.vsr.configuration;

import com.botoni.vsr.properties.JwtProperties;
import com.botoni.vsr.security.JwtToken;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Bean
    public JwtToken jwtToken(JwtProperties properties) {
        return new JwtToken(properties.secretKey(), properties.issuer(), properties.expirationTime());
    }
}
