package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.Individual;
import com.botoni.vsr.database.entity.LocalCredential;
import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.dto.request.RegisterRequest;
import com.botoni.vsr.dto.response.AuthenticationResponse;
import com.botoni.vsr.vo.PasswordHash;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final IndividualService individualService;
    private final UserService userService;
    private final LocalCredentialService localCredentialService;
    private final SessionService sessionService;
    private final AccessService accessService;

    @Transactional
    public AuthenticationResponse open(RegisterRequest request, PasswordHash hash, InetAddress ip) {
        Individual person = savePerson(request);
        User user = saveUser(person, request);
        saveCredential(user, hash);
        return grant(user, request, ip);
    }

    @Transactional
    public void replace(UUID user, Integer session, LocalCredential credential, PasswordHash hash) {
        replaceCredential(credential, hash);
        revokeOthers(user, session);
    }

    private Individual savePerson(RegisterRequest request) {
        return individualService.save(request.name(), request.cpf());
    }

    private User saveUser(Individual person, RegisterRequest request) {
        return userService.save(person, request.email());
    }

    private void saveCredential(User user, PasswordHash hash) {
        localCredentialService.save(user, hash);
    }

    private AuthenticationResponse grant(User user, RegisterRequest request, InetAddress ip) {
        return accessService.grant(user, request.device(), ip);
    }

    private void replaceCredential(LocalCredential credential, PasswordHash hash) {
        localCredentialService.replace(credential, hash);
    }

    private void revokeOthers(UUID user, Integer session) {
        sessionService.revokeOthers(user, session);
    }
}
