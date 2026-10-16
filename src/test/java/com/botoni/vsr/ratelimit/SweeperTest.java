package com.botoni.vsr.ratelimit;

import com.botoni.vsr.support.Controle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Limpeza de buckets ociosos")
class SweeperTest {

    private static final double START = 1_000;
    private static final double TTL = 30;

    private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final Sweeper sweeper = new Sweeper(new Limit(3, 1, Duration.ofSeconds(10)), buckets);

    @AfterEach
    void close() {
        sweeper.close();
    }

    @Test
    @Controle
    @DisplayName("remove só os buckets sem recarga há pelo menos o tempo de vida")
    void evictsOnlyIdleBuckets() {
        buckets.put("idle", new Bucket(0, START, false));
        buckets.put("active", new Bucket(0, START + 1, false));

        sweeper.evict(START + TTL);

        assertThat(buckets).containsOnlyKeys("active");
    }

    @Test
    @Controle
    @DisplayName("a limpeza sob demanda roda no máximo uma vez por segundo, para não varrer o mapa a cada requisição")
    void reliefIsThrottled() {
        buckets.put("first", new Bucket(0, START, false));
        sweeper.relieve(START + TTL);
        buckets.put("second", new Bucket(0, START, false));

        sweeper.relieve(START + TTL + 0.5);
        assertThat(buckets).containsKey("second");

        sweeper.relieve(START + TTL + 1);
        assertThat(buckets).isEmpty();
    }

    @Test
    @Controle
    @DisplayName("exatamente no tempo de vida o bucket já é ocioso; um instante antes, não")
    void ttlBoundary() {
        buckets.put("ip:10.0.0.1", new Bucket(0, START, false));

        sweeper.evict(START + TTL - 0.001);
        assertThat(buckets).containsKey("ip:10.0.0.1");

        sweeper.evict(START + TTL);
        assertThat(buckets).isEmpty();
    }

    @Test
    @Controle
    @DisplayName("o scheduler compartilhado remove os ociosos sozinho, sem chamar evict")
    void schedulerEvictsIdleBuckets() throws InterruptedException {
        ConcurrentMap<String, Bucket> idle = new ConcurrentHashMap<>();
        try (Sweeper fast = new Sweeper(new Limit(1, 1, Duration.ofMillis(1)), idle)) {
            idle.put("ip:10.0.0.1", new Bucket(0, TokenBucket.now(), false));

            assertThat(awaitEmpty(idle)).isTrue();
        }
    }

    @Test
    @Controle
    @DisplayName("vários limites compartilham o scheduler e fechar um não afeta os outros")
    void sweepersShareScheduler() throws InterruptedException {
        ConcurrentMap<String, Bucket> first = new ConcurrentHashMap<>();
        ConcurrentMap<String, Bucket> second = new ConcurrentHashMap<>();
        Sweeper closed = new Sweeper(new Limit(1, 1, Duration.ofMillis(1)), first);
        try (Sweeper open = new Sweeper(new Limit(1, 1, Duration.ofMillis(1)), second)) {
            closed.close();
            second.put("ip:10.0.0.1", new Bucket(0, TokenBucket.now(), false));

            assertThat(awaitEmpty(second)).isTrue();
        }
    }

    private static boolean awaitEmpty(ConcurrentMap<String, Bucket> map) throws InterruptedException {
        long deadline = System.nanoTime() + Duration.ofSeconds(5).toNanos();
        while (System.nanoTime() < deadline) {
            if (map.isEmpty()) {
                return true;
            }
            Thread.sleep(50);
        }
        return map.isEmpty();
    }

    @Test
    @Controle
    @DisplayName("fechar descarta todos os buckets")
    void closeClearsBuckets() {
        buckets.put("ip:10.0.0.1", new Bucket(1, START, false));

        sweeper.close();

        assertThat(buckets).isEmpty();
    }
}
