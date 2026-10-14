package com.botoni.vsr.ratelimit;

import com.botoni.vsr.exception.custom.RateLimitException;
import com.botoni.vsr.exception.enums.problem.RateLimitProblem;
import com.botoni.vsr.attempt.Attempt;
import com.botoni.vsr.support.Controle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Tentativas")
class AttemptLimiterTest {

    private static final int CAPACITY = 3;
    private static final Attempt ATTEMPT = Attempt.of("login:ana@vsr.com", Rejected.class);
    private static final Attempt OTHER = Attempt.of("password:3f1c9a52", Rejected.class);

    private final AttemptLimiter attemptLimiter = AttemptLimiter.from(new Limit(CAPACITY, 1, Duration.ofHours(1)));

    @AfterEach
    void close() {
        attemptLimiter.close();
    }

    @Test
    @Controle
    @DisplayName("a exceção de rejeição informada mantém a tentativa gasta; ao estourar, a ação nem roda")
    void rejectionsExhaustKey() {
        AtomicInteger runs = new AtomicInteger();
        Supplier<String> rejected = () -> {
            runs.incrementAndGet();
            throw new Rejected();
        };
        fail(rejected, CAPACITY, Rejected.class);

        assertThatThrownBy(() -> attemptLimiter.attempt(ATTEMPT, rejected))
                .isInstanceOf(RateLimitException.class)
                .extracting("problem").isEqualTo(RateLimitProblem.EXCEEDED);
        assertThat(runs.get()).isEqualTo(CAPACITY);
    }

    @Test
    @Controle
    @DisplayName("qualquer outra exceção devolve a tentativa e passa adiante, inclusive falha de infraestrutura")
    void otherExceptionsRefund() {
        fail(() -> {
            throw new DataAccessResourceFailureException("banco indisponível");
        }, CAPACITY * 3, DataAccessResourceFailureException.class);
        fail(() -> {
            throw new IllegalStateException("outra regra");
        }, CAPACITY * 3, IllegalStateException.class);

        assertThat(attemptLimiter.attempt(ATTEMPT, () -> "ok")).isEqualTo("ok");
    }

    @Test
    @Controle
    @DisplayName("sucesso devolve a tentativa e repassa o resultado")
    void successRefunds() {
        for (int i = 0; i < CAPACITY * 3; i++) {
            assertThat(attemptLimiter.attempt(ATTEMPT, () -> "ok")).isEqualTo("ok");
        }
    }

    @Test
    @Controle
    @DisplayName("chaves diferentes não dividem tentativas")
    void keysAreIndependent() {
        fail(() -> {
            throw new Rejected();
        }, CAPACITY, Rejected.class);

        assertThat(attemptLimiter.attempt(OTHER, () -> "ok")).isEqualTo("ok");
    }

    private void fail(Supplier<String> action, int times, Class<? extends Throwable> expected) {
        for (int i = 0; i < times; i++) {
            assertThatThrownBy(() -> attemptLimiter.attempt(ATTEMPT, action)).isInstanceOf(expected);
        }
    }

    private static final class Rejected extends RuntimeException {
    }
}
