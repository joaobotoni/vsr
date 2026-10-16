package com.botoni.vsr.ratelimit;

import com.botoni.vsr.ratelimit.TokenBucket.Limit;
import com.botoni.vsr.support.Controle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import com.botoni.vsr.ratelimit.TokenBucket.Allowance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

@DisplayName("Saldos de um limite")
class TokenBucketTest {

    private static final int CAPACITY = 3;
    private static final double START = 1_000;
    private static final double INTERVAL = 10;
    private static final double TTL = 30;
    private static final String KEY = "ip:10.0.0.1";
    private static final Limit LIMIT = new Limit(CAPACITY, 1, Duration.ofSeconds((long) INTERVAL));

    private final double[] clock = {START};
    private final ConcurrentMap<String, TokenBucket.Bucket> buckets = new ConcurrentHashMap<>();
    private final TokenBucket tokenBucket = new TokenBucket(LIMIT, 1_000, buckets);

    @AfterEach
    void close() {
        tokenBucket.close();
    }

    @Nested
    @DisplayName("consumo")
    class Consume {

        @Test
        @Controle
        @DisplayName("cada consumo gasta uma ficha e informa o saldo restante")
        void consumeSpendsOneToken() {
            assertThat(tokenBucket.allow(KEY, clock[0])).isEqualTo(new Allowance(true, 2, INTERVAL));
            assertThat(tokenBucket.allow(KEY, clock[0])).isEqualTo(new Allowance(true, 1, INTERVAL));
            assertThat(tokenBucket.allow(KEY, clock[0])).isEqualTo(new Allowance(true, 0, INTERVAL));
        }

        @Test
        @Controle
        @DisplayName("sem fichas, o consumo é recusado e informa quando volta a recarregar")
        void exhaustedKeyIsRejected() {
            exhaust();
            clock[0] = START + 4;

            Allowance allowance = tokenBucket.allow(KEY, clock[0]);

            assertThat(allowance.allowed()).isFalse();
            assertThat(allowance.remaining()).isZero();
            assertThat(TokenBucket.retry(allowance)).isEqualTo(6);
        }

        @Test
        @Controle
        @DisplayName("a recusa não gasta ficha: recarrega no próximo intervalo")
        void rejectionDoesNotSpend() {
            exhaust();
            clock[0] = START + 1;
            tokenBucket.allow(KEY, clock[0]);
            clock[0] = START + INTERVAL;

            assertThat(tokenBucket.allow(KEY, clock[0]).allowed()).isTrue();
        }

        @Test
        @Controle
        @DisplayName("cada intervalo completo recarrega a taxa configurada, sem passar da capacidade")
        void refillIsPerFullIntervalAndCapped() {
            exhaust();

            clock[0] = START + INTERVAL - 1;
            assertThat(tokenBucket.allow(KEY, clock[0]).allowed()).isFalse();
            clock[0] = START + 2 * INTERVAL;
            assertThat(tokenBucket.allow(KEY, clock[0]).remaining()).isEqualTo(1);
            clock[0] = START + 100 * INTERVAL;
            assertThat(tokenBucket.allow(KEY, clock[0]).remaining()).isEqualTo(CAPACITY - 1);
        }

        @Test
        @Controle
        @DisplayName("a recarga parcial conta a partir do último intervalo completo")
        void refillKeepsPartialInterval() {
            exhaust();
            clock[0] = START + INTERVAL + 4;

            assertThat(TokenBucket.retry(tokenBucket.allow(KEY, clock[0]))).isEqualTo(6);
        }

        @Test
        @Controle
        @DisplayName("chaves diferentes têm saldos independentes")
        void keysAreIndependent() {
            exhaust();

            assertThat(tokenBucket.allow("ip:10.0.0.2", clock[0]).allowed()).isTrue();
        }

        @Test
        @Controle
        @DisplayName("a recusa informa um tempo de nova tentativa dentro do intervalo, no relógio real")
        void rejectionRetryWithinInterval() {
            try (TokenBucket real = new TokenBucket(new Limit(1, 1, Duration.ofMinutes(1)))) {
                real.allow(KEY);

                assertThat(TokenBucket.retry(real.allow(KEY))).isBetween(1L, 60L);
            }
        }
    }

    @Nested
    @DisplayName("recarga")
    class Refill {

        @ParameterizedTest(name = "{0}s depois de esvaziar → {1} fichas, próxima recarga em {2}s")
        @Controle
        @DisplayName("só intervalos completos contam, e a recarga avança em múltiplos do intervalo")
        @CsvSource({
                "0,     0, 10",
                "9.999, 0, 0.001",
                "10,    1, 10",
                "19.9,  1, 0.1",
                "25,    2, 5",
                "30,    3, 10",
                "1000,  3, 10"
        })
        void refillsWholeIntervals(double after, double tokens, double reset) {
            exhaust();
            clock[0] = START + after;

            Allowance allowance = tokenBucket.allow(KEY, clock[0]);

            assertThat(allowance.allowed()).isEqualTo(tokens >= 1);
            assertThat(allowance.remaining()).isEqualTo(Math.max(0, tokens - 1));
            assertThat(allowance.reset()).isCloseTo(reset, offset(1e-9));
        }

        @Test
        @Controle
        @DisplayName("taxa fracionária acumula até completar uma ficha")
        void fractionalRateAccumulates() {
            try (TokenBucket slow = new TokenBucket(new Limit(2, 0.5, Duration.ofSeconds(10)), 1_000, new ConcurrentHashMap<>())) {
                slow.allow(KEY, clock[0]);
                slow.allow(KEY, clock[0]);

                clock[0] = START + 10;
                assertThat(slow.allow(KEY, clock[0]).allowed()).isFalse();
                clock[0] = START + 20;
                assertThat(slow.allow(KEY, clock[0]).allowed()).isTrue();
            }
        }

        @Test
        @Controle
        @DisplayName("período em milissegundos é respeitado")
        void millisecondPeriod() {
            try (TokenBucket fast = new TokenBucket(new Limit(4, 1, Duration.ofMillis(1500)), 1_000, new ConcurrentHashMap<>())) {
                for (int i = 0; i < 4; i++) {
                    fast.allow(KEY, clock[0]);
                }
                clock[0] = START + 4;

                Allowance allowance = fast.allow(KEY, clock[0]);

                assertThat(allowance.remaining()).isEqualTo(1);
                assertThat(allowance.reset()).isCloseTo(0.5, offset(1e-9));
            }
        }

        @Test
        @Controle
        @DisplayName("saldo restante arredonda para baixo")
        void remainingFloors() {
            try (TokenBucket slow = new TokenBucket(new Limit(3, 0.5, Duration.ofSeconds(10)), 1_000, new ConcurrentHashMap<>())) {
                for (int i = 0; i < 3; i++) {
                    slow.allow(KEY, clock[0]);
                }
                clock[0] = START + 30;

                assertThat(slow.allow(KEY, clock[0]).remaining()).isZero();
            }
        }

        @Test
        @Controle
        @DisplayName("o tempo para encher é ceil(capacidade / taxa) intervalos: depois dele o saldo é esquecido")
        void ttlIsTimeToRefillCompletely() {
            ConcurrentMap<String, TokenBucket.Bucket> map = new ConcurrentHashMap<>();
            try (TokenBucket half = new TokenBucket(new Limit(5, 2, Duration.ofSeconds(10)), 1_000, map)) {
                half.allow(KEY, clock[0]);

                clock[0] = START + 29.999;
                half.evict(clock[0]);
                assertThat(map).containsKey(KEY);

                clock[0] = START + 30;
                half.evict(clock[0]);
                assertThat(map).isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("limite de chaves")
    class Crowding {

        private final TokenBucket small = new TokenBucket(LIMIT, 2, new ConcurrentHashMap<>());

        @AfterEach
        void close() {
            small.close();
        }

        @Test
        @Controle
        @DisplayName("cheio, recusa chave nova e as chaves já contadas seguem funcionando")
        void fullRejectsNewKeysOnly() {
            assertThat(small.allow("ip:10.0.0.1", START).allowed()).isTrue();
            assertThat(small.allow("ip:10.0.0.2", START).allowed()).isTrue();

            assertThat(small.allow("ip:10.0.0.3", START)).isEqualTo(new Allowance(false, 0, INTERVAL));
            assertThat(small.allow("ip:10.0.0.1", START).allowed()).isTrue();
        }

        @Test
        @Controle
        @DisplayName("cheio, apaga os saldos ociosos antes de recusar a chave nova")
        void fullReleasesIdleKeys() {
            small.allow("ip:10.0.0.1", START);
            small.allow("ip:10.0.0.2", START);

            assertThat(small.allow("ip:10.0.0.3", START + TTL).allowed()).isTrue();
        }

        @Test
        @Controle
        @DisplayName("a limpeza sob demanda roda no máximo uma vez por segundo")
        void reliefIsThrottled() {
            small.allow("ip:10.0.0.1", START);
            small.allow("ip:10.0.0.2", START);

            assertThat(small.allow("ip:10.0.0.3", START + TTL - 0.5).allowed()).isFalse();
            assertThat(small.allow("ip:10.0.0.3", START + TTL).allowed()).isFalse();
            assertThat(small.allow("ip:10.0.0.3", START + TTL + 0.5).allowed()).isTrue();
        }
    }

    @Nested
    @DisplayName("limpeza agendada")
    class Scheduling {

        @Test
        @Controle
        @DisplayName("o scheduler compartilhado apaga os ociosos sozinho, e fechar um limite não afeta os outros")
        void sharedSchedulerEvictsIdleKeys() throws InterruptedException {
            ConcurrentMap<String, TokenBucket.Bucket> second = new ConcurrentHashMap<>();
            TokenBucket closed = scheduled(new TokenBucket(new Limit(1, 1, Duration.ofMillis(1)), 1_000, new ConcurrentHashMap<>()));
            try (TokenBucket open = scheduled(new TokenBucket(new Limit(1, 1, Duration.ofMillis(1)), 1_000, second))) {
                closed.close();
                open.allow(KEY);

                assertThat(awaitEmpty(second)).isTrue();
            }
        }

        private TokenBucket scheduled(TokenBucket tokenBucket) {
            tokenBucket.schedule();
            return tokenBucket;
        }

        private boolean awaitEmpty(ConcurrentMap<String, TokenBucket.Bucket> map) throws InterruptedException {
            long deadline = System.nanoTime() + Duration.ofSeconds(5).toNanos();
            while (System.nanoTime() < deadline && !map.isEmpty()) {
                Thread.sleep(50);
            }
            return map.isEmpty();
        }
    }

    @Nested
    @DisplayName("limpeza")
    class Sweeping {

        @Test
        @Controle
        @DisplayName("a varredura remove só os saldos sem recarga há pelo menos o tempo para encher")
        void sweepEvictsOnlyIdleKeys() {
            tokenBucket.allow("idle", clock[0]);
            clock[0] = START + 1;
            tokenBucket.allow("active", clock[0]);
            clock[0] = START + TTL;

            tokenBucket.evict(clock[0]);

            assertThat(buckets).containsOnlyKeys("active");
        }

        @Test
        @Controle
        @DisplayName("exatamente no tempo para encher o saldo já é ocioso; um instante antes, não")
        void ttlBoundary() {
            tokenBucket.allow(KEY, clock[0]);

            clock[0] = START + TTL - 0.001;
            tokenBucket.evict(clock[0]);
            assertThat(buckets).containsKey(KEY);

            clock[0] = START + TTL;
            tokenBucket.evict(clock[0]);
            assertThat(buckets).isEmpty();
        }

        @Test
        @Controle
        @DisplayName("fechar descarta os saldos")
        void closeDiscardsBuckets() {
            exhaust();

            tokenBucket.close();

            assertThat(buckets).isEmpty();
            assertThat(tokenBucket.allow(KEY, clock[0]).allowed()).isTrue();
        }
    }

    @Nested
    @DisplayName("concorrência")
    class Concurrency {

        private static final int THREADS = 32;
        private static final int CALLS_PER_THREAD = 200;
        private static final int LARGE = 50;
        private static final Limit HOUR = new Limit(LARGE, 1, Duration.ofHours(1));

        private final ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        private final TokenBucket shared = new TokenBucket(HOUR, 1_000, new ConcurrentHashMap<>());

        @AfterEach
        void shutdown() throws InterruptedException {
            executor.shutdownNow();
            shared.close();
            assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
        }

        @RepeatedTest(20)
        @Controle
        @DisplayName("consumos simultâneos na mesma chave nunca liberam mais que a capacidade")
        void parallelConsumesRespectCapacity() throws Exception {
            AtomicInteger allowed = new AtomicInteger();

            run(() -> {
                for (int i = 0; i < CALLS_PER_THREAD; i++) {
                    allowed.addAndGet(shared.allow(KEY, START).allowed() ? 1 : 0);
                }
                return null;
            });

            assertThat(allowed.get()).isEqualTo(LARGE);
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
                    allowed[k].addAndGet(shared.allow("ip:10.0.1." + k, START).allowed() ? 1 : 0);
                }
                return null;
            });

            for (AtomicInteger count : allowed) {
                assertThat(count.get()).isEqualTo(LARGE);
            }
        }

        @RepeatedTest(20)
        @Controle
        @DisplayName("a limpeza simultânea ao consumo nunca apaga um saldo recém-gasto: a chave libera exatamente a capacidade")
        void sweepNeverLosesConcurrentConsumption() throws Exception {
            int keys = 64;
            double[] time = {START};
            TokenBucket racing = new TokenBucket(HOUR, 1_000, new ConcurrentHashMap<>());
            for (int k = 0; k < keys; k++) {
                racing.allow("ip:10.0.2." + k, time[0]);
            }
            time[0] = START + LARGE * 3600;
            AtomicBoolean consuming = new AtomicBoolean(true);
            AtomicInteger[] allowed = new AtomicInteger[keys];
            for (int k = 0; k < keys; k++) {
                allowed[k] = new AtomicInteger();
            }

            Future<?> sweeper = executor.submit(() -> {
                while (consuming.get()) {
                    racing.evict(time[0]);
                }
            });
            run(() -> {
                for (int k = 0; k < keys; k++) {
                    allowed[k].addAndGet(racing.allow("ip:10.0.2." + k, time[0]).allowed() ? 1 : 0);
                }
                return null;
            });
            consuming.set(false);
            sweeper.get(30, TimeUnit.SECONDS);

            for (int k = 0; k < keys; k++) {
                assertThat(allowed[k].get() + drain(racing, "ip:10.0.2." + k, time[0])).isEqualTo(LARGE);
            }
            racing.close();
        }

        private void run(Callable<Void> task) throws Exception {
            CountDownLatch start = new CountDownLatch(1);
            List<Future<Void>> futures = new ArrayList<>();
            for (int t = 0; t < THREADS - 1; t++) {
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

    private void exhaust() {
        for (int i = 0; i < CAPACITY; i++) {
            tokenBucket.allow(KEY, clock[0]);
        }
    }

    private static int drain(TokenBucket tokenBucket, String key, double now) {
        int allowed = 0;
        while (tokenBucket.allow(key, now).allowed()) {
            allowed++;
        }
        return allowed;
    }
}
