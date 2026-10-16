package com.botoni.vsr.ratelimit;

import com.botoni.vsr.support.Controle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Allowance do rate limit")
class AllowanceTest {

    @ParameterizedTest(name = "reset {0}s → nova tentativa em {1}s")
    @Controle
    @DisplayName("o tempo para nova tentativa arredonda para cima e é de pelo menos um segundo")
    @CsvSource({"-5, 1", "0, 1", "0.001, 1", "1, 1", "1.001, 2", "6, 6", "59.5, 60"})
    void retryRoundsUpWithMinimum(double reset, long retry) {
        assertThat(new Allowance(false, 0, reset).retry()).isEqualTo(retry);
    }

    @Test
    @Controle
    @DisplayName("excedido é o oposto de permitido")
    void exceededIsNotAllowed() {
        assertThat(new Allowance(true, 2, 1).exceeded()).isFalse();
        assertThat(new Allowance(false, 0, 1).exceeded()).isTrue();
    }
}
