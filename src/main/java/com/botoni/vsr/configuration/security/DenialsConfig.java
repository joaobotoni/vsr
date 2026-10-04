package com.botoni.vsr.configuration.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
@RequiredArgsConstructor
public class DenialsConfig {

    private final HandlerExceptionResolver handlerExceptionResolver;

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return this::resolve;
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return this::resolve;
    }

    private void resolve(HttpServletRequest request, HttpServletResponse response, Exception exception) {
        handlerExceptionResolver.resolveException(request, response, null, exception);
    }
}
