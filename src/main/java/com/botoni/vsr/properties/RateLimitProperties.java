package com.botoni.vsr.properties;

import com.botoni.vsr.exception.custom.LimitException;
import com.botoni.vsr.exception.enums.problem.LimitProblem;
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
        @NotNull Limit ceiling,
        @NotNull Limit password
) {

    public RateLimitProperties {
        if (isLow(account, ceiling)) {
            throw new LimitException(LimitProblem.CEILING_TOO_LOW, ceiling.capacity(), account.capacity());
        }
    }

    private static boolean isLow(Limit account, Limit ceiling) {
        return account != null && ceiling != null && ceiling.capacity() <= account.capacity();
    }
}
