package com.botoni.vsr.properties;

import com.botoni.vsr.exception.custom.SessionException;
import com.botoni.vsr.exception.enums.problem.SessionProblem;
import com.botoni.vsr.support.Controle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Tempos da sessão")
class SessionPropertiesTest {

    private static final int MAX = 10;

    @Controle
    @ParameterizedTest(name = "validade {0} e inatividade {1} impedem a subida ({2})")
    @DisplayName("validade ou inatividade zero, negativa ou inatividade que não cabe na validade é recusada")
    @CsvSource(delimiter = '|', value = {
            "PT0S   | P7D    | INVALID_TTL",
            "-P30D  | P7D    | INVALID_TTL",
            "P30D   | PT0S   | INVALID_IDLE",
            "P30D   | -P7D   | INVALID_IDLE",
            "P30D   | P30D   | IDLE_TOO_LONG",
            "P7D    | P30D   | IDLE_TOO_LONG"
    })
    void incoherentDurationsAreRejected(String ttl, String idle, SessionProblem problem) {
        assertThatThrownBy(() -> new SessionProperties(Duration.parse(ttl), Duration.parse(idle), MAX))
                .isInstanceOf(SessionException.class)
                .extracting("problem").isEqualTo(problem);
    }

    @Test
    @Controle
    @DisplayName("inatividade menor que a validade é aceita")
    void idleWithinTtlIsAccepted() {
        assertThat(new SessionProperties(Duration.ofDays(30), Duration.ofDays(7), MAX).idle()).isEqualTo(Duration.ofDays(7));
    }
}
