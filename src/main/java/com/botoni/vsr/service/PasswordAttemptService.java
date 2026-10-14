package com.botoni.vsr.service;

import com.botoni.vsr.attempt.Attempt;
import com.botoni.vsr.attempt.IAttempt;
import com.botoni.vsr.database.entity.LocalCredential;
import com.botoni.vsr.exception.custom.CredentialException;
import com.botoni.vsr.vo.Password;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordAttemptService {

    private static final String KEY = "password:%s";

    private final IAttempt attempts;
    private final LocalCredentialService localCredentialService;

    public LocalCredential verify(UUID user, Password password) {
        return attempts.attempt(attempt(user), () -> credential(user, password));
    }

    private static Attempt attempt(UUID user) {
        return Attempt.of(key(user), CredentialException.class);
    }

    private static String key(UUID user) {
        return String.format(KEY, user);
    }

    private LocalCredential credential(UUID user, Password password) {
        return localCredentialService.verify(user, password);
    }
}
