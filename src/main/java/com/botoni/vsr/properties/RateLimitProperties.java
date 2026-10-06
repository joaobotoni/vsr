package com.botoni.vsr.properties;

import com.botoni.vsr.ratelimit.Limit;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "rate-limit")
public record RateLimitProperties(
        @Valid @NotNull Limit api,
        @Valid @NotNull Limit login,
        @Valid @NotNull Limit register,
        @Valid @NotNull Limit refresh,
        @Valid @NotNull Limit password,
        @Valid @NotNull Limit upload
) {
}
