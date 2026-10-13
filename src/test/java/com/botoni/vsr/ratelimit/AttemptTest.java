package com.botoni.vsr.ratelimit;

import com.botoni.vsr.support.Controle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tentativa")
class AttemptTest {

    private final Attempt attempt = Attempt.of("login:ana@vsr.com", IllegalArgumentException.class);

    @Test
    @Controle
    @DisplayName("a exceção de rejeição e suas subclasses são rejeição")
    void rejectionAndSubclassesAreRejected() {
        assertThat(attempt.rejects(new IllegalArgumentException())).isTrue();
        assertThat(attempt.rejects(new NumberFormatException())).isTrue();
    }

    @Test
    @Controle
    @DisplayName("qualquer outra exceção não é rejeição")
    void otherExceptionsAreNotRejected() {
        assertThat(attempt.rejects(new IllegalStateException())).isFalse();
    }
}
