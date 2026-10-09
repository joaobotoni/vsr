package com.botoni.vsr.configuration;

import com.botoni.vsr.properties.JwtProperties;
import com.botoni.vsr.token.JwtToken;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class JwtConfig {

    @Bean
    public JwtToken jwtToken(JwtProperties properties, Clock clock) {
        return new JwtToken(properties.secretKey(), properties.issuer(), properties.expirationTime(), clock);
    }
}
