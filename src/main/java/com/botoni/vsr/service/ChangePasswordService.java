package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.LocalCredential;
import com.botoni.vsr.dto.response.TokenResponse;
import com.botoni.vsr.vo.Password;
import com.botoni.vsr.vo.PasswordHash;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChangePasswordService {

    private final PasswordAttemptService passwordAttemptService;
    private final LocalCredentialService localCredentialService;
    private final PasswordService passwordService;

    public TokenResponse change(UUID user, Integer session, Password currentPassword, Password newPassword, InetAddress ip) {
        LocalCredential credential = verify(user, currentPassword);
        PasswordHash hash = rehash(credential, newPassword);
        return commit(user, session, credential, hash, ip);
    }

    private LocalCredential verify(UUID user, Password password) {
        return passwordAttemptService.verify(user, password);
    }

    private PasswordHash rehash(LocalCredential credential, Password password) {
        return localCredentialService.rehash(credential, password);
    }

    private TokenResponse commit(UUID user, Integer session, LocalCredential credential, PasswordHash hash, InetAddress ip) {
        return passwordService.commit(user, session, credential, hash, ip);
    }
}
