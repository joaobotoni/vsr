package com.botoni.vsr.filter;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.botoni.vsr.service.auth.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class AuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (!hasBearerToken(request) || isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }
        authenticate(request);
        filterChain.doFilter(request, response);
    }

    private void authenticate(HttpServletRequest request) {
        try {
            UserDetails user = loadUserByToken(getToken(request));
            setAuthentication(authenticationToken(user));
        } catch (JWTVerificationException | UsernameNotFoundException exception) {
            SecurityContextHolder.clearContext();
        }
    }

    private UserDetails loadUserByToken(String token) {
        return userDetailsService.loadUserByUsername(jwtService.subject(token));
    }

    private static UsernamePasswordAuthenticationToken authenticationToken(UserDetails user) {
        return UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities());
    }

    private static void setAuthentication(UsernamePasswordAuthenticationToken authentication) {
        SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
    }

    private static String getToken(HttpServletRequest request) {
        return request.getHeader(HttpHeaders.AUTHORIZATION).substring(BEARER_PREFIX.length());
    }

    private static boolean hasBearerToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        return header != null && header.startsWith(BEARER_PREFIX);
    }

    private static boolean isAuthenticated() {
        return SecurityContextHolder.getContext().getAuthentication() != null;
    }
}