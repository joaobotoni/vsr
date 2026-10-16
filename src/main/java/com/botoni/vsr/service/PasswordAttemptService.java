package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.LocalCredential;
import com.botoni.vsr.exception.custom.RateLimitException;
import com.botoni.vsr.exception.enums.problem.RateLimitProblem;
import com.botoni.vsr.ratelimit.RateLimitPolicy;
import com.botoni.vsr.ratelimit.TokenBucket;
import com.botoni.vsr.ratelimit.TokenBucket.Allowance;
import com.botoni.vsr.vo.Password;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordAttemptService {

    private static final String KEY = "password:%s";

    private final RateLimitPolicy rateLimitPolicy;
    private final LocalCredentialService localCredentialService;

    public LocalCredential verify(UUID user, Password password) {
        Allowance allowance = allow(user);
        within(allowance);
        return credential(user, password);
    }

    private Allowance allow(UUID user) {
        return rateLimitPolicy.account().allow(key(user));
    }

    private static String key(UUID user) {
        return String.format(KEY, user);
    }

    private LocalCredential credential(UUID user, Password password) {
        return localCredentialService.verify(user, password);
    }

    private static void within(Allowance allowance) {
        if (!allowance.allowed()) {
            throw new RateLimitException(RateLimitProblem.EXCEEDED, TokenBucket.retry(allowance));
        }
    }
}
