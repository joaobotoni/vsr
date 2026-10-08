package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.dto.request.LoginRequest;
import com.botoni.vsr.dto.response.AuthenticationResponse;
import com.botoni.vsr.principal.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.net.InetAddress;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final LoginAttemptService loginAttemptService;
    private final UserService userService;
    private final AccessService accessService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse login(LoginRequest request, InetAddress ip) {
        checkAttempts(request);
        Authentication authentication = authenticate(request);
        User user = find(authentication);
        return grant(user, request, ip);
    }

    private void checkAttempts(LoginRequest request) {
        loginAttemptService.check(request.email());
    }

    private Authentication authenticate(LoginRequest request) {
        try {
            return authenticationManager.authenticate(credentials(request));
        } catch (AuthenticationException exception) {
            registerFailure(request);
            throw exception;
        }
    }

    private void registerFailure(LoginRequest request) {
        loginAttemptService.fail(request.email());
    }

    private User find(Authentication authentication) {
        return userService.findWithPerson(principal(authentication).user());
    }

    private AuthenticationResponse grant(User user, LoginRequest request, InetAddress ip) {
        return accessService.grant(user, request.device(), ip);
    }

    private static Principal principal(Authentication authentication) {
        return (Principal) authentication.getPrincipal();
    }

    private static UsernamePasswordAuthenticationToken credentials(LoginRequest request) {
        return UsernamePasswordAuthenticationToken.unauthenticated(request.email().value(), request.password().value());
    }
}
