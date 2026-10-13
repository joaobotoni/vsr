package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.dto.request.LoginRequest;
import com.botoni.vsr.dto.response.AuthenticationResponse;
import com.botoni.vsr.principal.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.net.InetAddress;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final LoginAttemptService loginAttemptService;
    private final UserService userService;
    private final AccessService accessService;

    public AuthenticationResponse login(LoginRequest request, InetAddress ip) {
        Authentication authentication = verify(request);
        User user = find(authentication);
        return authenticate(user, request, ip);
    }

    private Authentication verify(LoginRequest request) {
        return loginAttemptService.verify(request.email(), request.password(), request.device().identifier());
    }

    private User find(Authentication authentication) {
        return userService.find(principal(authentication).user());
    }

    private static Principal principal(Authentication authentication) {
        return (Principal) authentication.getPrincipal();
    }

    private AuthenticationResponse authenticate(User user, LoginRequest request, InetAddress ip) {
        return accessService.authenticate(user, request.device(), ip);
    }
}
