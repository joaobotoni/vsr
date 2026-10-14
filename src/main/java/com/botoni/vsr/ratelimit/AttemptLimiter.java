package com.botoni.vsr.ratelimit;

import com.botoni.vsr.attempt.Attempt;
import com.botoni.vsr.attempt.IAttempt;
import com.botoni.vsr.exception.custom.RateLimitException;
import com.botoni.vsr.exception.enums.problem.RateLimitProblem;

import java.util.function.Supplier;

public final class AttemptLimiter implements IAttempt, AutoCloseable {

    private final Rule rule;

    private AttemptLimiter(Rule rule) {
        this.rule = rule;
    }

    public static AttemptLimiter from(Limit limit) {
        return new AttemptLimiter(Rule.any(limit));
    }

    @Override
    public <T> T attempt(Attempt attempt, Supplier<T> action) {
        within(consume(attempt));
        T result = run(attempt, action);
        refund(attempt);
        return result;
    }

    @Override
    public void close() {
        rule.close();
    }

    private Verdict consume(Attempt attempt) {
        return rule.consume(attempt.key());
    }

    private void refund(Attempt attempt) {
        rule.refund(attempt.key());
    }

    private void within(Verdict verdict) {
        if (verdict.exceeded()) {
            throw new RateLimitException(RateLimitProblem.EXCEEDED, verdict.retry());
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
        if (attempt.fails(exception)) {
            return;
        }
        refund(attempt);
    }
}
