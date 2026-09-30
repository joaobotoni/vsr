package com.botoni.vsr.configuration.properties;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "rate-limit")
public record RateLimitProperties(
        @Positive int capacity,
        @Positive double refillRate,
        @NotNull Duration refillInterval
) {

    public double refillIntervalSeconds() {
        return refillInterval.toNanos() / 1_000_000_000.0;
    }
}
