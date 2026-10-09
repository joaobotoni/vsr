package com.botoni.vsr.ratelimit;

import com.botoni.vsr.exception.custom.LimitException;
import com.botoni.vsr.exception.enums.problem.LimitProblem;

import java.time.Duration;

public record Limit(int capacity, double refillRate, Duration refillInterval) {

    public Limit {
        if (capacity <= 0) {
            throw new LimitException(LimitProblem.INVALID_CAPACITY);
        }
        if (refillRate <= 0) {
            throw new LimitException(LimitProblem.INVALID_REFILL_RATE);
        }
        if (refillInterval == null || !isPositive(refillInterval)) {
            throw new LimitException(LimitProblem.INVALID_REFILL_INTERVAL);
        }
    }

    public double seconds() {
        return refillInterval.toMillis() / 1000.0;
    }

    private static boolean isPositive(Duration interval) {
        return !interval.isZero() && !interval.isNegative();
    }
}
