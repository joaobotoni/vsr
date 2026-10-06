package com.botoni.vsr.ratelimit;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Duration;

public record Limit(@Positive int capacity, @Positive double refillRate, @NotNull Duration refillInterval) {
}
