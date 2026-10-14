package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.Device;
import com.botoni.vsr.database.entity.LocalCredential;
import com.botoni.vsr.dto.response.TokenResponse;
import com.botoni.vsr.vo.PasswordHash;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordService {

    private final LocalCredentialService localCredentialService;
    private final SessionService sessionService;
    private final AccessService accessService;

    @Transactional
    public TokenResponse commit(UUID user, Integer session, LocalCredential credential, PasswordHash hash, InetAddress ip) {
        Device device = revokeAll(user, session);
        replace(credential, hash);
        return rotate(device, ip);
    }

    private Device revokeAll(UUID user, Integer session) {
        return sessionService.revokeAll(user, session);
    }

    private void replace(LocalCredential credential, PasswordHash hash) {
        localCredentialService.replace(credential, hash);
    }

    private TokenResponse rotate(Device device, InetAddress ip) {
        return accessService.rotate(device, ip);
    }
}
