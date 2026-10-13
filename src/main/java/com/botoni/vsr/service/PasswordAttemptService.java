package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.LocalCredential;
import com.botoni.vsr.exception.custom.CredentialException;
import com.botoni.vsr.ratelimit.Attempt;
import com.botoni.vsr.vo.Password;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordAttemptService {

    private static final String KEY = "password:%s";

    private final AttemptService attemptService;
    private final LocalCredentialService localCredentialService;

    public LocalCredential verify(UUID user, Password password) {
        return attemptService.attempt(attempt(user), () -> credential(user, password));
    }

    private static Attempt attempt(UUID user) {
        return Attempt.of(String.format(KEY, user), CredentialException.class);
    }

    private LocalCredential credential(UUID user, Password password) {
        return localCredentialService.verify(user, password);
    }
}
