package com.botoni.vsr.attempt;

import com.botoni.vsr.support.Controle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tentativa")
class AttemptTest {

    private final Attempt attempt = Attempt.of("login:ana@vsr.com", IllegalArgumentException.class);

    @Test
    @Controle
    @DisplayName("a exceção de falha e suas subclasses são falha")
    void failureAndSubclassesFail() {
        assertThat(attempt.fails(new IllegalArgumentException())).isTrue();
        assertThat(attempt.fails(new NumberFormatException())).isTrue();
    }

    @Test
    @Controle
    @DisplayName("qualquer outra exceção não é falha")
    void otherExceptionsDoNotFail() {
        assertThat(attempt.fails(new IllegalStateException())).isFalse();
    }
}
