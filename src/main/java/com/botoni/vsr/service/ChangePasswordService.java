package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.LocalCredential;
import com.botoni.vsr.vo.Password;
import com.botoni.vsr.vo.PasswordHash;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChangePasswordService {

    private final LocalCredentialService localCredentialService;
    private final PasswordService passwordService;

    public void change(UUID user, Integer session, Password currentPassword, Password newPassword) {
        LocalCredential credential = verify(user, currentPassword);
        PasswordHash hash = renew(credential, newPassword);
        replace(user, session, credential, hash);
    }

    private LocalCredential verify(UUID user, Password password) {
        return localCredentialService.verify(user, password);
    }

    private PasswordHash renew(LocalCredential credential, Password password) {
        return localCredentialService.renew(credential, password);
    }

    private void replace(UUID user, Integer session, LocalCredential credential, PasswordHash hash) {
        passwordService.replace(user, session, credential, hash);
    }
}