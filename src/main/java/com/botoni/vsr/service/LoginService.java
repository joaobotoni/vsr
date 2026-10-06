package com.botoni.vsr.service;

import com.botoni.vsr.dto.request.LoginRequest;
import com.botoni.vsr.dto.response.AuthenticationResponse;
import com.botoni.vsr.dto.response.TokenResponse;
import com.botoni.vsr.mapper.AuthenticationMapper;
import com.botoni.vsr.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final AccessService accessService;
    private final AuthenticationManager authenticationManager;
    private final AuthenticationMapper authenticationMapper;

    @Transactional
    public AuthenticationResponse login(LoginRequest request, InetAddress ip) {
        Principal principal = authenticate(request);
        TokenResponse token = grant(principal, request, ip);
        return respond(principal, token);
    }

    private Principal authenticate(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(credentials(request));
        return (Principal) authentication.getPrincipal();
    }

    private TokenResponse grant(Principal principal, LoginRequest request, InetAddress ip) {
        return accessService.grant(principal, request.device(), ip);
    }

    private AuthenticationResponse respond(Principal principal, TokenResponse token) {
        return authenticationMapper.toResponse(principal.user(), token);
    }

    private static UsernamePasswordAuthenticationToken credentials(LoginRequest request) {
        return UsernamePasswordAuthenticationToken.unauthenticated(request.email().value(), request.password().value());
    }
}
