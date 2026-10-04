package com.botoni.vsr.service.password;

import com.botoni.vsr.entity.users.LocalCredential;
import com.botoni.vsr.exception.infrastructure.CredentialException;
import com.botoni.vsr.repository.LocalCredentialRepository;
import com.botoni.vsr.vo.Password;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChangePasswordService {

    private final LocalCredentialRepository localCredentialRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void changePassword(Integer userId, String currentPassword, Password newPassword) {
        LocalCredential credential = verified(userId, currentPassword);
        change(credential, newPassword);
    }

    private LocalCredential verified(Integer userId, String password) {
        return find(userId).filter(credential -> matches(credential, password))
                .orElseThrow(CredentialException.IncorrectCurrentPassword::new);
    }

    private Optional<LocalCredential> find(Integer userId) {
        return localCredentialRepository.findById(userId);
    }

    private boolean matches(LocalCredential credential, String password) {
        return credential.getPasswordHash().matches(password, passwordEncoder);
    }

    private void change(LocalCredential credential, Password password) {
        credential.setPasswordHash(password.encodeWith(passwordEncoder));
        credential.setPasswordUpdatedAt(Instant.now());
    }
}