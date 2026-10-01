package com.botoni.vsr.service;

import com.botoni.vsr.entity.LocalCredential;
import com.botoni.vsr.exception.infrastructure.CredentialException;
import com.botoni.vsr.repository.LocalCredentialRepository;
import com.botoni.vsr.vo.Password;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChangePasswordService {

    private final LocalCredentialRepository localCredentialRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void changePassword(Integer userId, Password currentPassword, Password newPassword) {
        LocalCredential credential = localCredentialRepository.findById(userId)
                .orElseThrow(CredentialException.IncorrectCurrentPassword::new);

        if (!credential.matches(currentPassword, passwordEncoder)) {
            throw new CredentialException.IncorrectCurrentPassword();
        }

        credential.changePassword(newPassword, passwordEncoder);
    }
}
