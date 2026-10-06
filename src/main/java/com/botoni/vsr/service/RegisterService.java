package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.LocalCredential;
import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.dto.request.RegisterRequest;
import com.botoni.vsr.dto.response.AuthenticationResponse;
import com.botoni.vsr.dto.response.TokenResponse;
import com.botoni.vsr.mapper.AuthenticationMapper;
import com.botoni.vsr.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;

@Service
@RequiredArgsConstructor
public class RegisterService {

    private final AccessService accessService;
    private final UserService userService;
    private final LocalCredentialService localCredentialService;
    private final AuthenticationMapper authenticationMapper;

    @Transactional
    public AuthenticationResponse register(RegisterRequest request, InetAddress ip) {
        User user = saveUser(request);
        Principal principal = saveCredential(user, request);
        TokenResponse token = grant(principal, request, ip);
        return respond(principal, token);
    }

    private User saveUser(RegisterRequest request) {
        return userService.save(request);
    }

    private Principal saveCredential(User user, RegisterRequest request) {
        LocalCredential credential = localCredentialService.save(user, request.password());
        return Principal.from(credential);
    }

    private TokenResponse grant(Principal principal, RegisterRequest request, InetAddress ip) {
        return accessService.grant(principal, request.device(), ip);
    }

    private AuthenticationResponse respond(Principal principal, TokenResponse token) {
        return authenticationMapper.toResponse(principal.user(), token);
    }
}
