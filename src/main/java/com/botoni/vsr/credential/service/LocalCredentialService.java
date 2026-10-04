package com.botoni.vsr.credential.service;

import com.botoni.vsr.bundle.LocalCredentialBundle;
import com.botoni.vsr.credential.entity.LocalCredential;
import com.botoni.vsr.credential.mapper.LocalCredentialMapper;
import com.botoni.vsr.credential.repository.LocalCredentialRepository;
import com.botoni.vsr.shared.vo.Password;
import com.botoni.vsr.shared.vo.PasswordHash;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LocalCredentialService {

    private final LocalCredentialRepository localCredentialRepository;
    private final LocalCredentialMapper localCredentialMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public LocalCredential create(LocalCredentialBundle bundle) {
        return localCredentialRepository.save(localCredentialMapper.toEntity(bundle, encode(bundle.password())));
    }

    private PasswordHash encode(Password password) {
        return PasswordHash.of(passwordEncoder.encode(password.value()));
    }
}
