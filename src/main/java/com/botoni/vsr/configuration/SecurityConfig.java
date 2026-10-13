package com.botoni.vsr.configuration;

import com.botoni.vsr.filter.AuthenticationFilter;
import com.botoni.vsr.filter.ExceptionFilter;
import com.botoni.vsr.filter.RateLimitFilter;
import com.botoni.vsr.filter.RequestBodySizeLimitFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;

import java.time.Duration;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final long HSTS_MAX_AGE = Duration.ofDays(365).toSeconds();

    public static final String[] PUBLIC_ENDPOINTS = {
            "/api/*/auth/login",
            "/api/*/auth/register",
            "/api/*/auth/refresh",
            "/error"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationEntryPoint authenticationEntryPoint,
                                                   AccessDeniedHandler accessDeniedHandler,
                                                   ExceptionFilter exceptionFilter,
                                                   RateLimitFilter rateLimitFilter,
                                                   AuthenticationFilter authenticationFilter,
                                                   RequestBodySizeLimitFilter sizeLimitFilter
    ) {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.httpStrictTransportSecurity(hsts -> hsts
                        .requestMatcher(AnyRequestMatcher.INSTANCE)
                        .includeSubDomains(true)
                        .maxAgeInSeconds(HSTS_MAX_AGE)))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(rateLimitFilter, AuthenticationFilter.class)
                .addFilterBefore(exceptionFilter, RateLimitFilter.class)
                .addFilterAfter(sizeLimitFilter, RateLimitFilter.class)
                .build();
    }
}
