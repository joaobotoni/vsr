package com.botoni.vsr.ratelimit;

import com.botoni.vsr.exception.custom.LimitException;
import com.botoni.vsr.exception.enums.problem.LimitProblem;

import java.time.Duration;

public record Limit(int capacity, double rate, Duration period) {

    private static final double MILLIS_PER_SECOND = 1000.0;

    public Limit {
        if (invalid(capacity)) {
            throw new LimitException(LimitProblem.INVALID_CAPACITY);
        }
        if (invalid(rate)) {
            throw new LimitException(LimitProblem.INVALID_REFILL_RATE);
        }
        if (empty(period)) {
            throw new LimitException(LimitProblem.INVALID_REFILL_INTERVAL);
        }
    }

    double interval() {
        return period.toMillis() / MILLIS_PER_SECOND;
    }

    double ttl() {
        return refills() * interval();
    }

    private double refills() {
        return Math.ceil(capacity / rate);
    }

    private static boolean invalid(double value) {
        return value <= 0;
    }

    private static boolean empty(Duration period) {
        return period == null || period.isZero() || period.isNegative();
    }
}
