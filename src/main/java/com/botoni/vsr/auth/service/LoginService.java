package com.botoni.vsr.auth.service;

import com.botoni.vsr.auth.dto.request.LoginRequest;
import com.botoni.vsr.bundle.SignInBundle;
import com.botoni.vsr.bundle.SessionBundle;
import com.botoni.vsr.auth.dto.response.LoginResponse;
import com.botoni.vsr.token.dto.response.TokenResponse;
import com.botoni.vsr.auth.mapper.LoginMapper;
import com.botoni.vsr.bundle.mapper.AccessBundleMapper;
import com.botoni.vsr.infra.security.Principal;
import com.botoni.vsr.session.service.AccessService;
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
    private final AccessBundleMapper accessBundleMapper;

    @Transactional
    public LoginResponse login(SignInBundle bundle) {
        Principal principal = authenticate(bundle.login());
        TokenResponse token = grant(principal, bundle.session());
        return respond(principal, token);
    }

    private Principal authenticate(LoginRequest request) {
        return (Principal) authenticationManager.authenticate(credentials(request)).getPrincipal();
    }

    private TokenResponse grant(Principal principal, SessionBundle session) {
        return accessService.grant(accessBundleMapper.toBundle(principal, session));
    }

    private LoginResponse respond(Principal principal, TokenResponse token) {
        return loginMapper.toResponse(principal.user(), token);
    }

    private static UsernamePasswordAuthenticationToken credentials(LoginRequest request) {
        return UsernamePasswordAuthenticationToken.unauthenticated(request.email().value(), request.password());
    }
}
