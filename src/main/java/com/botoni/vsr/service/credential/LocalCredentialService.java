package com.botoni.vsr.service.credential;

import com.botoni.vsr.command.credential.LocalCredentialCommand;
import com.botoni.vsr.entity.users.LocalCredential;
import com.botoni.vsr.mapper.credential.LocalCredentialMapper;
import com.botoni.vsr.repository.LocalCredentialRepository;
import com.botoni.vsr.vo.Password;
import com.botoni.vsr.vo.PasswordHash;
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
    public LocalCredential create(LocalCredentialCommand command) {
        return localCredentialRepository.save(localCredentialMapper.toEntity(command, encode(command.password())));
    }

    private PasswordHash encode(Password password) {
        return password.encodeWith(passwordEncoder);
    }
}
