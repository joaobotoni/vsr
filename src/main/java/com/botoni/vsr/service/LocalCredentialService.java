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

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocalCredentialService {

    private final LocalCredentialRepository localCredentialRepository;
    private final LocalCredentialMapper localCredentialMapper;
    private final PasswordEncoder passwordEncoder;

    public PasswordHash hash(Password password) {
        return PasswordHash.of(passwordEncoder.encode(password.value()));
    }

    @Transactional
    public void save(User user, PasswordHash hash) {
        LocalCredential credential = create(user, hash);
        persist(credential);
    }

    public LocalCredential verify(UUID user, Password password) {
        return matched(find(user), password);
    }

    public PasswordHash rehash(LocalCredential credential, Password password) {
        return hash(unmatched(credential, password));
    }

    @Transactional
    public void replace(LocalCredential credential, PasswordHash hash) {
        localCredentialRepository.change(credential.getId(), credential.getPasswordHash().value(), hash.value());
    }

    private LocalCredential create(User user, PasswordHash hash) {
        return localCredentialMapper.toEntity(user, hash);
    }

    private void persist(LocalCredential credential) {
        localCredentialRepository.save(credential);
    }

    private LocalCredential find(UUID user) {
        return localCredentialRepository.findByUserUuid(user)
                .orElseThrow(() -> new CredentialException(CredentialProblem.NOT_FOUND));
    }

    private LocalCredential matched(LocalCredential credential, Password password) {
        if (!matches(credential, password)) {
            throw new CredentialException(CredentialProblem.INCORRECT_CURRENT_PASSWORD);
        }
        return credential;
    }

    private Password unmatched(LocalCredential credential, Password password) {
        if (matches(credential, password)) {
            throw new CredentialException(CredentialProblem.SAME_PASSWORD);
        }
        return password;
    }

    private boolean matches(LocalCredential credential, Password password) {
        return passwordEncoder.matches(password.value(), credential.getPasswordHash().value());
    }
}
