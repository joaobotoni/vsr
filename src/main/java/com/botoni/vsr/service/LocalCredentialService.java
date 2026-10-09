package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.LocalCredential;
import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.database.repository.LocalCredentialRepository;
import com.botoni.vsr.exception.custom.CredentialException;
import com.botoni.vsr.exception.enums.problem.CredentialProblem;
import com.botoni.vsr.mapper.LocalCredentialMapper;
import com.botoni.vsr.vo.Password;
import com.botoni.vsr.vo.PasswordHash;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocalCredentialService {

    private final LocalCredentialRepository localCredentialRepository;
    private final LocalCredentialMapper localCredentialMapper;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public PasswordHash hash(Password password) {
        return PasswordHash.of(passwordEncoder.encode(password.value()));
    }

    @Transactional
    public void save(User user, PasswordHash hash) {
        localCredentialRepository.save(create(user, hash));
    }

    public LocalCredential verify(UUID user, Password password) {
        LocalCredential credential = find(user);
        matched(credential, password);
        return credential;
    }

    public PasswordHash renew(LocalCredential credential, Password password) {
        unmatched(credential, password);
        return hash(password);
    }

    @Transactional
    public void replace(LocalCredential credential, PasswordHash hash) {
        localCredentialRepository.change(credential.getId(), credential.getPasswordHash().value(), hash.value());
    }

    private LocalCredential create(User user, PasswordHash hash) {
        return localCredentialMapper.entity(user, hash, Instant.now(clock));
    }

    private LocalCredential find(UUID user) {
        return localCredentialRepository.findByUserUuid(user)
                .orElseThrow(() -> new CredentialException(CredentialProblem.NOT_FOUND));
    }

    private void matched(LocalCredential credential, Password password) {
        if (!isCurrent(credential, password)) {
            throw new CredentialException(CredentialProblem.INCORRECT_CURRENT_PASSWORD);
        }
    }

    private void unmatched(LocalCredential credential, Password password) {
        if (isCurrent(credential, password)) {
            throw new CredentialException(CredentialProblem.SAME_PASSWORD);
        }
    }

    private boolean isCurrent(LocalCredential credential, Password password) {
        return passwordEncoder.matches(password.value(), credential.getPasswordHash().value());
    }
}