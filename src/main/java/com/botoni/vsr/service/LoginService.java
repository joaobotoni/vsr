package com.botoni.vsr.service;

import com.botoni.vsr.dto.request.LoginRequest;
import com.botoni.vsr.dto.response.LoginResponse;
import com.botoni.vsr.entity.User;
import com.botoni.vsr.mapper.LoginMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final TokenService tokenService;
    private final LoginMapper loginMapper;
    private final AuthenticationManager authenticationManager;

    public LoginResponse login(LoginRequest request) {
        User user = authenticate(request);
        return loginMapper.toResponse(user, tokenService.issue(user));
    }

    private User authenticate(LoginRequest request) {
        return (User) authenticationManager.authenticate(credentials(request)).getPrincipal();
    }

    private static UsernamePasswordAuthenticationToken credentials(LoginRequest request) {
        return UsernamePasswordAuthenticationToken.unauthenticated(request.email().value(), request.password().value());
    }
}