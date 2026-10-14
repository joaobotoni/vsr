package com.botoni.vsr.ratelimit;

import com.botoni.vsr.attempt.Attempt;
import com.botoni.vsr.exception.custom.RateLimitException;
import com.botoni.vsr.support.Controle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tentativas sob concorrência")
class AttemptLimiterConcurrencyTest {

    private static final int THREADS = 32;
    private static final int CAPACITY = 5;
    private static final Attempt ATTEMPT = Attempt.of("login:ana@vsr.com", Failure.class);

    private final ExecutorService executor = Executors.newFixedThreadPool(THREADS);
    private final AttemptLimiter attemptLimiter = AttemptLimiter.from(new Limit(CAPACITY, 1, Duration.ofHours(1)));

    @AfterEach
    void close() throws InterruptedException {
        executor.shutdownNow();
        attemptLimiter.close();
        assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
    }

    @RepeatedTest(20)
    @Controle
    @DisplayName("falhas simultâneas só executam a ação até a capacidade; o resto é bloqueado")
    void parallelFailuresRespectCapacity() throws Exception {
        AtomicInteger runs = new AtomicInteger();
        AtomicInteger blocked = new AtomicInteger();

        parallel(() -> {
            runs.incrementAndGet();
            throw new Failure();
        }, blocked);

        assertThat(runs.get()).isEqualTo(CAPACITY);
        assertThat(blocked.get()).isEqualTo(THREADS - CAPACITY);
    }

    @RepeatedTest(20)
    @Controle
    @DisplayName("sucessos e falhas que não contam devolvem a tentativa: nenhum token se perde")
    void parallelSuccessesLoseNoToken() throws Exception {
        AtomicInteger blocked = new AtomicInteger();
        AtomicInteger runs = new AtomicInteger();

        parallel(() -> "ok", blocked);
        parallel(() -> {
            throw new IllegalStateException("outra regra");
        }, blocked);
        for (int i = 0; i <= CAPACITY; i++) {
            attempt(() -> {
                runs.incrementAndGet();
                throw new Failure();
            }, new AtomicInteger());
        }

        assertThat(runs.get()).isEqualTo(CAPACITY);
    }

    private void parallel(Supplier<String> action, AtomicInteger blocked) throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();
        for (int t = 0; t < THREADS; t++) {
            futures.add(executor.submit(() -> {
                start.await();
                attempt(action, blocked);
                return null;
            }));
        }
        start.countDown();
        for (Future<?> future : futures) {
            future.get(30, TimeUnit.SECONDS);
        }
    }

    private void attempt(Supplier<String> action, AtomicInteger blocked) {
        try {
            attemptLimiter.attempt(ATTEMPT, action);
        } catch (RateLimitException exception) {
            blocked.incrementAndGet();
        } catch (RuntimeException ignored) {
            // falha da própria ação: já contabilizada pelo teste
        }
    }

    private static final class Failure extends RuntimeException {
    }
}
