package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.Individual;
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
    private final IndividualService individualService;
    private final UserService userService;
    private final LocalCredentialService localCredentialService;
    private final AuthenticationMapper authenticationMapper;

    @Transactional
    public AuthenticationResponse register(RegisterRequest request, InetAddress ip) {
        Individual person = savePerson(request);
        User user = saveUser(person, request);
        LocalCredential credential = saveCredential(user, request);
        Principal principal = principal(credential);
        TokenResponse token = grant(principal, request, ip);
        return respond(principal, token);
    }

    private Individual savePerson(RegisterRequest request) {
        return individualService.save(request.name(), request.cpf());
    }

    private User saveUser(Individual person, RegisterRequest request) {
        return userService.save(person, request.email());
    }

    private LocalCredential saveCredential(User user, RegisterRequest request) {
        return localCredentialService.save(user, request.password());
    }

    private TokenResponse grant(Principal principal, RegisterRequest request, InetAddress ip) {
        return accessService.grant(principal, request.device(), ip);
    }

    private AuthenticationResponse respond(Principal principal, TokenResponse token) {
        return authenticationMapper.toResponse(principal.user(), token);
    }

    private static Principal principal(LocalCredential credential) {
        return Principal.from(credential);
    }
}
