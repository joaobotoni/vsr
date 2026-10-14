package com.botoni.vsr.ratelimit;

import com.botoni.vsr.support.Controle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Token bucket sob concorrência")
class TokenBucketConcurrencyTest {

    private static final int THREADS = 32;
    private static final int CALLS_PER_THREAD = 200;
    private static final int CAPACITY = 50;
    private static final double NOW = 1_000;
    private static final String KEY = "ip:10.0.0.1";

    private final ExecutorService executor = Executors.newFixedThreadPool(THREADS);
    private final TokenBucket bucket =
            new TokenBucket(new Limit(CAPACITY, 1, Duration.ofHours(1)), new ConcurrentHashMap<>());

    @AfterEach
    void shutdown() throws InterruptedException {
        executor.shutdownNow();
        assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
    }

    @RepeatedTest(20)
    @Controle
    @DisplayName("consumos simultâneos na mesma chave nunca liberam mais que a capacidade")
    void parallelConsumesRespectCapacity() throws Exception {
        AtomicInteger allowed = new AtomicInteger();

        run(() -> {
            for (int i = 0; i < CALLS_PER_THREAD; i++) {
                if (bucket.take(KEY, NOW).allowed()) {
                    allowed.incrementAndGet();
                }
            }
            return null;
        });

        assertThat(allowed.get()).isEqualTo(CAPACITY);
    }

    @RepeatedTest(20)
    @Controle
    @DisplayName("com consumo e devolução simultâneos, nunca há mais tokens em uso que a capacidade nem tokens perdidos")
    void parallelConsumeAndRefundKeepInvariant() throws Exception {
        AtomicInteger inUse = new AtomicInteger();
        AtomicInteger peak = new AtomicInteger();

        run(() -> {
            for (int i = 0; i < CALLS_PER_THREAD; i++) {
                if (bucket.take(KEY, NOW).allowed()) {
                    peak.accumulateAndGet(inUse.incrementAndGet(), Math::max);
                    inUse.decrementAndGet();
                    bucket.refund(KEY);
                }
            }
            return null;
        });

        assertThat(peak.get()).isBetween(1, CAPACITY);
        assertThat(drain(KEY)).isEqualTo(CAPACITY);
    }

    @Test
    @Controle
    @DisplayName("chaves diferentes consumidas em paralelo têm saldos independentes")
    void parallelKeysAreIndependent() throws Exception {
        int keys = 8;
        AtomicInteger[] allowed = new AtomicInteger[keys];
        for (int k = 0; k < keys; k++) {
            allowed[k] = new AtomicInteger();
        }

        run(() -> {
            for (int i = 0; i < CALLS_PER_THREAD; i++) {
                int k = i % keys;
                if (bucket.take("ip:10.0.1." + k, NOW).allowed()) {
                    allowed[k].incrementAndGet();
                }
            }
            return null;
        });

        for (AtomicInteger count : allowed) {
            assertThat(count.get()).isEqualTo(CAPACITY);
        }
    }

    @Test
    @Controle
    @DisplayName("devoluções simultâneas em excesso não passam da capacidade")
    void parallelRefundsAreCapped() throws Exception {
        bucket.take(KEY, NOW);

        run(() -> {
            for (int i = 0; i < CALLS_PER_THREAD; i++) {
                bucket.refund(KEY);
            }
            return null;
        });

        assertThat(drain(KEY)).isEqualTo(CAPACITY);
    }

    private int drain(String key) {
        int allowed = 0;
        while (bucket.take(key, NOW).allowed()) {
            allowed++;
        }
        return allowed;
    }

    private void run(Callable<Void> task) throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Void>> futures = new ArrayList<>();
        for (int t = 0; t < THREADS; t++) {
            futures.add(executor.submit(() -> {
                start.await();
                return task.call();
            }));
        }
        start.countDown();
        for (Future<Void> future : futures) {
            future.get(30, TimeUnit.SECONDS);
        }
    }
}
