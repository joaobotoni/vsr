package com.botoni.vsr.filter;

import com.botoni.vsr.configuration.SecurityConfig;
import com.botoni.vsr.security.JwtToken;
import com.botoni.vsr.security.Principal;
import com.botoni.vsr.service.SessionService;
import com.botoni.vsr.service.TokenService;
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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class AuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER = "Bearer ";

    private static final RequestMatcher PUBLIC = matcher(SecurityConfig.PUBLIC_ENDPOINTS);

    private final TokenService tokenService;
    private final UserDetailsService userDetailsService;
    private final SessionService sessionService;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return PUBLIC.matches(request);
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        authenticateIfPossible(request);
        chain.doFilter(request, response);
    }

    private void authenticateIfPossible(HttpServletRequest request) {
        if (!isAuthenticatable(request)) {
            return;
        }
        authenticate(request);
    }

    private void authenticate(HttpServletRequest request) {
        JwtToken.Claims claims = verify(request);
        Principal principal = load(claims);
        access(principal, claims);
        store(principal, claims);
    }

    private JwtToken.Claims verify(HttpServletRequest request) {
        return tokenService.verify(token(request));
    }

    private Principal load(JwtToken.Claims claims) {
        return (Principal) userDetailsService.loadUserByUsername(claims.subject());
    }

    private void access(Principal principal, JwtToken.Claims claims) {
        sessionService.access(principal.user(), claims.session());
    }

    private static void store(Principal principal, JwtToken.Claims claims) {
        Principal authenticated = principal.withSession(claims.session());
        SecurityContextHolder.setContext(new SecurityContextImpl(authentication(authenticated)));
    }

    private static UsernamePasswordAuthenticationToken authentication(Principal principal) {
        return UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities());
    }

    private static boolean isAuthenticatable(HttpServletRequest request) {
        return hasToken(request) && !isAuthenticated();
    }

    private static boolean hasToken(HttpServletRequest request) {
        String header = header(request);
        return header != null && header.startsWith(BEARER);
    }

    private static boolean isAuthenticated() {
        return SecurityContextHolder.getContext().getAuthentication() != null;
    }

    private static String token(HttpServletRequest request) {
        return header(request).substring(BEARER.length());
    }

    private static String header(HttpServletRequest request) {
        return request.getHeader(HttpHeaders.AUTHORIZATION);
    }

    private static RequestMatcher matcher(String... patterns) {
        return new OrRequestMatcher(matchers(patterns));
    }

    private static RequestMatcher[] matchers(String... patterns) {
        RequestMatcher[] matchers = new RequestMatcher[patterns.length];
        for (int i = 0; i < patterns.length; i++) {
            matchers[i] = path(patterns[i]);
        }
        return matchers;
    }

    private static RequestMatcher path(String pattern) {
        return PathPatternRequestMatcher.withDefaults().matcher(pattern);
    }
}
