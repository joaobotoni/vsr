package com.botoni.vsr.service;

import com.botoni.vsr.exception.custom.RateLimitException;
import com.botoni.vsr.exception.enums.problem.RateLimitProblem;
import com.botoni.vsr.ratelimit.Quota;
import com.botoni.vsr.ratelimit.RateLimit;
import com.botoni.vsr.vo.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private static final String KEY = "login:%s";

    private final RateLimit accountRateLimit;

    public void check(Email email) {
        exceeded(quota(email));
    }

    public void fail(Email email) {
        accountRateLimit.consume(key(email));
    }

    private Quota quota(Email email) {
        return accountRateLimit.check(key(email));
    }

    private static String key(Email email) {
        return String.format(KEY, email.value());
    }

    private void exceeded(Quota quota) {
        if (quota.isExceeded()) {
            throw new RateLimitException(RateLimitProblem.EXCEEDED, quota.retry());
        }
    }
}