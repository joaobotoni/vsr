package com.botoni.vsr.ratelimit;

import com.botoni.vsr.exception.custom.LimitException;
import com.botoni.vsr.exception.enums.problem.LimitProblem;
import com.botoni.vsr.support.Controle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Limite do rate limit")
class LimitTest {

    @Test
    @Controle
    @DisplayName("limite válido guarda o intervalo em segundos")
    void validLimitIsAccepted() {
        assertThat(new Limit(5, 1, Duration.ofMillis(1500)).seconds()).isEqualTo(1.5);
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
        assertThatThrownBy(() -> new Limit(capacity, refillRate, interval))
                .isInstanceOf(LimitException.class)
                .extracting("problem").isEqualTo(problem);
    }

    @Test
    @Controle
    @DisplayName("intervalo ausente impede a subida")
    void missingIntervalIsRejected() {
        assertThatThrownBy(() -> new Limit(5, 1, null))
                .isInstanceOf(LimitException.class)
                .extracting("problem").isEqualTo(LimitProblem.INVALID_REFILL_INTERVAL);
    }
}
