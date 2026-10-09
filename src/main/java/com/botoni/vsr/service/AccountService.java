package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.Individual;
import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.dto.request.RegisterRequest;
import com.botoni.vsr.dto.response.AuthenticationResponse;
import com.botoni.vsr.vo.PasswordHash;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final IndividualService individualService;
    private final UserService userService;
    private final LocalCredentialService localCredentialService;
    private final AccessService accessService;

    @Transactional
    public AuthenticationResponse open(RegisterRequest request, PasswordHash hash, InetAddress ip) {
        Individual person = person(request);
        User user = user(person, request);
        secure(user, hash);
        return authenticate(user, request, ip);
    }

    private Individual person(RegisterRequest request) {
        return individualService.save(request.name(), request.cpf());
    }

    private User user(Individual person, RegisterRequest request) {
        return userService.save(person, request.email());
    }

    private void secure(User user, PasswordHash hash) {
        localCredentialService.save(user, hash);
    }

    private AuthenticationResponse authenticate(User user, RegisterRequest request, InetAddress ip) {
        return accessService.authenticate(user, request.device(), ip);
    }
}