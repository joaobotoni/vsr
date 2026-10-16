package com.botoni.vsr.ratelimit;

import com.botoni.vsr.exception.custom.LimitException;
import com.botoni.vsr.exception.enums.problem.LimitProblem;
import com.botoni.vsr.ratelimit.TokenBucket.Limit;
import com.botoni.vsr.support.Controle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Validação do limite do rate limit")
class LimitTest {

    @Test
    @Controle
    @DisplayName("limite válido monta o token bucket")
    void validLimitIsAccepted() {
        Limit limit = new Limit(5, 0.5, Duration.ofMillis(1500));

        assertThat(bucket(limit).limit()).isEqualTo(limit);
    }

    @Controle
    @ParameterizedTest(name = "{0} impede a subida ({4})")
    @CsvSource(delimiter = '|', value = {
            "capacidade zero         | 0 | 1  | PT1S  | INVALID_CAPACITY",
            "capacidade negativa     | -1| 1  | PT1S  | INVALID_CAPACITY",
            "taxa de recarga zero    | 5 | 0  | PT1S  | INVALID_REFILL_RATE",
            "intervalo zero          | 5 | 1  | PT0S  | INVALID_REFILL_INTERVAL",
            "intervalo negativo      | 5 | 1  | -PT1S | INVALID_REFILL_INTERVAL"
    })
    void invalidLimitIsRejected(String description, int capacity, double refillRate, Duration interval, LimitProblem problem) {
        assertThatThrownBy(() -> bucket(new Limit(capacity, refillRate, interval)))
                .isInstanceOf(LimitException.class)
                .extracting("problem").isEqualTo(problem);
    }

    @Test
    @Controle
    @DisplayName("intervalo ausente impede a subida")
    void missingIntervalIsRejected() {
        assertThatThrownBy(() -> bucket(new Limit(5, 1, null)))
                .isInstanceOf(LimitException.class)
                .extracting("problem").isEqualTo(LimitProblem.INVALID_REFILL_INTERVAL);
    }

    private static TokenBucket bucket(Limit limit) {
        return new TokenBucket(limit, 1_000, new ConcurrentHashMap<>());
    }
}
