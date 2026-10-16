package com.botoni.vsr.properties;

import com.botoni.vsr.exception.custom.SessionException;
import com.botoni.vsr.exception.enums.problem.SessionProblem;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "security.session")
public record SessionProperties(@NotNull Duration ttl, @NotNull Duration idle, @Positive int max) {

    public SessionProperties {
        if (empty(ttl)) {
            throw new SessionException(SessionProblem.INVALID_TTL);
        }
        if (empty(idle)) {
            throw new SessionException(SessionProblem.INVALID_IDLE);
        }
        if (outlasts(idle, ttl)) {
            throw new SessionException(SessionProblem.IDLE_TOO_LONG, idle, ttl);
        }
    }

    private static boolean empty(Duration duration) {
        return duration == null || duration.isZero() || duration.isNegative();
    }

    private static boolean outlasts(Duration idle, Duration ttl) {
        return idle.compareTo(ttl) >= 0;
    }
}
