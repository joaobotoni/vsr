package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.LocalCredential;
import com.botoni.vsr.vo.PasswordHash;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordService {

    private final LocalCredentialService localCredentialService;
    private final SessionService sessionService;

    @Transactional
    public void replace(UUID user, Integer session, LocalCredential credential, PasswordHash hash) {
        keep(user, session);
        change(credential, hash);
    }

    private void keep(UUID user, Integer session) {
        sessionService.keep(user, session);
    }

    private void change(LocalCredential credential, PasswordHash hash) {
        localCredentialService.replace(credential, hash);
    }
}
