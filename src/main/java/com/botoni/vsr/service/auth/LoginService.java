package com.botoni.vsr.service.auth;

import com.botoni.vsr.dto.request.auth.LoginRequest;
import com.botoni.vsr.command.auth.SignInCommand;
import com.botoni.vsr.command.session.SessionCommand;
import com.botoni.vsr.dto.response.LoginResponse;
import com.botoni.vsr.dto.response.TokenResponse;
import com.botoni.vsr.mapper.auth.LoginMapper;
import com.botoni.vsr.mapper.session.AccessMapper;
import com.botoni.vsr.security.Principal;
import com.botoni.vsr.service.session.AccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final AccessService accessService;
    private final AuthenticationManager authenticationManager;
    private final LoginMapper loginMapper;
    private final AccessMapper accessMapper;

    @Transactional
    public LoginResponse login(SignInCommand command) {
        Principal principal = authenticate(command.login());
        TokenResponse token = grant(principal, command.session());
        return respond(principal, token);
    }

    private Principal authenticate(LoginRequest request) {
        return (Principal) authenticationManager.authenticate(credentials(request)).getPrincipal();
    }

    private TokenResponse grant(Principal principal, SessionCommand session) {
        return accessService.grant(accessMapper.toCommand(principal, session));
    }

    private LoginResponse respond(Principal principal, TokenResponse token) {
        return loginMapper.toResponse(principal.user(), token);
    }

    private static UsernamePasswordAuthenticationToken credentials(LoginRequest request) {
        return UsernamePasswordAuthenticationToken.unauthenticated(request.email().value(), request.password());
    }
}
