package com.botoni.vsr.properties;

import com.botoni.vsr.ratelimit.Limit;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "rate-limit")
public record RateLimitProperties(
        @NotNull Limit api,
        @NotNull Limit login,
        @NotNull Limit register,
        @NotNull Limit refresh,
        @NotNull Limit account,
        @NotNull Limit password,
        @NotNull Limit upload
) {
}
