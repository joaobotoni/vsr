package com.botoni.vsr.service;

import com.botoni.vsr.exception.custom.RateLimitException;
import com.botoni.vsr.exception.enums.problem.RateLimitProblem;
import com.botoni.vsr.lib.TokenBucket.RateLimitResult;
import com.botoni.vsr.ratelimit.RateLimit;
import com.botoni.vsr.vo.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private static final String KEY = "login:%s";

    private final RateLimit accountRateLimit;

    public RateLimitResult check(Email email) {
        return allowed(peek(email));
    }

    public void fail(Email email) {
        accountRateLimit.consume(key(email));
    }

    private RateLimitResult peek(Email email) {
        return accountRateLimit.check(key(email));
    }

    private static RateLimitResult allowed(RateLimitResult result) {
        if (result.exceeded()) {
            throw new RateLimitException(RateLimitProblem.EXCEEDED, result.retryAfter());
        }
        return result;
    }

    private static String key(Email email) {
        return String.format(KEY, email.value());
    }
}
