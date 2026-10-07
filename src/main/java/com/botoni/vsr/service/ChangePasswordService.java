package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.LocalCredential;
import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.vo.Password;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChangePasswordService {

    private final LocalCredentialService localCredentialService;
    private final SessionService sessionService;

    @Transactional
    public void change(User user, Integer session, Password currentPassword, Password newPassword) {
        LocalCredential credential = verify(user, currentPassword);
        update(credential, newPassword);
        revokeOthers(user, session);
    }

    private LocalCredential verify(User user, Password password) {
        return localCredentialService.verify(user, password);
    }

    private void update(LocalCredential credential, Password password) {
        localCredentialService.update(credential, password);
    }

    private void revokeOthers(User user, Integer session) {
        sessionService.revokeOthers(user, session);
    }
}
