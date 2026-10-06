package com.botoni.vsr.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
public class DenialsConfig {

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint(HandlerExceptionResolver handlerExceptionResolver) {
        return (request, response, exception) ->
                handlerExceptionResolver.resolveException(request, response, null, exception);
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler(HandlerExceptionResolver handlerExceptionResolver) {
        return (request, response, exception) ->
                handlerExceptionResolver.resolveException(request, response, null, exception);
    }
}
