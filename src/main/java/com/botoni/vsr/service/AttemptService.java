package com.botoni.vsr.service;

import com.botoni.vsr.exception.custom.RateLimitException;
import com.botoni.vsr.exception.enums.problem.RateLimitProblem;
import com.botoni.vsr.ratelimit.Attempt;
import com.botoni.vsr.ratelimit.Quota;
import com.botoni.vsr.ratelimit.RateLimit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class AttemptService {

    private final RateLimit accountRateLimit;

    public <T> T attempt(Attempt attempt, Supplier<T> action) {
        exceeded(quota(attempt));
        T result = run(attempt, action);
        refund(attempt);
        return result;
    }

    private Quota quota(Attempt attempt) {
        return accountRateLimit.consume(attempt.key());
    }

    private void refund(Attempt attempt) {
        accountRateLimit.refund(attempt.key());
    }

    private void exceeded(Quota quota) {
        if (quota.isExceeded()) {
            throw new RateLimitException(RateLimitProblem.EXCEEDED, quota.retry());
        }
    }

    private <T> T run(Attempt attempt, Supplier<T> action) {
        try {
            return action.get();
        } catch (RuntimeException exception) {
            release(attempt, exception);
            throw exception;
        }
    }

    private void release(Attempt attempt, RuntimeException exception) {
        if (attempt.rejects(exception)) {
            return;
        }
        refund(attempt);
    }
}
