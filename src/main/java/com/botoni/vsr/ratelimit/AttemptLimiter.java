package com.botoni.vsr.ratelimit;

import com.botoni.vsr.attempt.Attempt;
import com.botoni.vsr.attempt.IAttempt;
import com.botoni.vsr.exception.custom.RateLimitException;
import com.botoni.vsr.exception.enums.problem.RateLimitProblem;

import java.util.function.Supplier;

public final class AttemptLimiter implements IAttempt, AutoCloseable {

    private static final Rule UNCEILED = null;

    private final Rule rule;
    private final Rule ceiling;

    private AttemptLimiter(Rule rule, Rule ceiling) {
        this.rule = rule;
        this.ceiling = ceiling;
    }

    public static AttemptLimiter from(Limit limit) {
        return new AttemptLimiter(Rule.any(limit), UNCEILED);
    }

    public static AttemptLimiter celled(Limit limit, Limit ceiling) {
        return new AttemptLimiter(Rule.any(limit), Rule.any(ceiling));
    }

    @Override
    public <T> T attempt(Attempt attempt, Supplier<T> action) {
        within(consume(attempt));
        ceil(attempt);
        T result = run(attempt, action);
        refund(attempt);
        return result;
    }

    @Override
    public void close() {
        rule.close();
        shut();
    }

    private Allowance consume(Attempt attempt) {
        return rule.consume(attempt.key());
    }

    private void refund(Attempt attempt) {
        rule.refund(attempt.key());
        restore(attempt);
    }

    private void within(Allowance allowance) {
        if (allowance.exceeded()) {
            throw new RateLimitException(RateLimitProblem.EXCEEDED, allowance.retry());
        }
    }

    private void ceil(Attempt attempt) {
        if (!bounded(attempt)) {
            return;
        }
        beneath(attempt, ceiling.consume(attempt.ceiling()));
    }

    private void beneath(Attempt attempt, Allowance allowance) {
        if (allowance.exceeded()) {
            rule.refund(attempt.key());
            throw new RateLimitException(RateLimitProblem.EXCEEDED, allowance.retry());
        }
    }

    private void restore(Attempt attempt) {
        if (!bounded(attempt)) {
            return;
        }
        ceiling.refund(attempt.ceiling());
    }

    private void shut() {
        if (!covered()) {
            return;
        }
        ceiling.close();
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

    private boolean bounded(Attempt attempt) {
        return attempt.limited() && covered();
    }

    private boolean covered() {
        return ceiling != UNCEILED;
    }
}
