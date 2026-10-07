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

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class LocalCredentialService {

    private final LocalCredentialRepository localCredentialRepository;
    private final LocalCredentialMapper localCredentialMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public LocalCredential save(User user, Password password) {
        LocalCredential credential = create(user, password);
        return persist(credential);
    }

    @Transactional(readOnly = true)
    public LocalCredential verify(User user, Password password) {
        return matched(find(user), password);
    }

    @Transactional
    public void update(LocalCredential credential, Password password) {
        if (matches(credential, password)) {
            throw new CredentialException(CredentialProblem.SAME_PASSWORD);
        }
        change(credential, password);
        persist(credential);
    }

    private LocalCredential create(User user, Password password) {
        return localCredentialMapper.toEntity(user, hash(password));
    }

    private LocalCredential persist(LocalCredential credential) {
        return localCredentialRepository.save(credential);
    }

    private LocalCredential find(User user) {
        return localCredentialRepository.findById(user.getId())
                .orElseThrow(() -> new CredentialException(CredentialProblem.NOT_FOUND));
    }

    private LocalCredential matched(LocalCredential credential, Password password) {
        if (!matches(credential, password)) {
            throw new CredentialException(CredentialProblem.INCORRECT_CURRENT_PASSWORD);
        }
        return credential;
    }

    private void change(LocalCredential credential, Password password) {
        credential.setPasswordHash(hash(password));
        credential.setPasswordUpdatedAt(Instant.now());
    }

    private boolean matches(LocalCredential credential, Password password) {
        return passwordEncoder.matches(password.value(), credential.getPasswordHash().value());
    }

    private PasswordHash hash(Password password) {
        return PasswordHash.of(passwordEncoder.encode(password.value()));
    }
}
