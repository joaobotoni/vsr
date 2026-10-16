package com.botoni.vsr.ratelimit;

import com.botoni.vsr.support.Controle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Token bucket")
class TokenBucketTest {

    private static final int CAPACITY = 3;
    private static final String KEY = "ip:10.0.0.1";
    private static final double START = 1_000;
    private static final double INTERVAL = 10;

    private final TokenBucket bucket =
            new TokenBucket(new Limit(CAPACITY, 1, Duration.ofSeconds((long) INTERVAL)), new ConcurrentHashMap<>());

    @Test
    @Controle
    @DisplayName("cada consumo gasta um token e informa o saldo restante")
    void consumeSpendsOneToken() {
        assertThat(bucket.take(KEY, START)).isEqualTo(new Allowance(true, 2, INTERVAL));
        assertThat(bucket.take(KEY, START)).isEqualTo(new Allowance(true, 1, INTERVAL));
        assertThat(bucket.take(KEY, START)).isEqualTo(new Allowance(true, 0, INTERVAL));
    }

    @Test
    @Controle
    @DisplayName("sem tokens, o consumo é recusado e informa quando volta a recarregar")
    void exhaustedBucketIsRejected() {
        exhaust();

        Allowance allowance = bucket.take(KEY, START + 4);

        assertThat(allowance.exceeded()).isTrue();
        assertThat(allowance.remaining()).isZero();
        assertThat(allowance.retry()).isEqualTo(6);
    }

    @Test
    @Controle
    @DisplayName("a recusa não gasta token: recarrega no próximo intervalo")
    void rejectionDoesNotSpend() {
        exhaust();
        bucket.take(KEY, START + 1);

        assertThat(bucket.take(KEY, START + INTERVAL).allowed()).isTrue();
    }

    @Test
    @Controle
    @DisplayName("cada intervalo completo recarrega a taxa configurada, sem passar da capacidade")
    void refillIsPerFullIntervalAndCapped() {
        exhaust();

        assertThat(bucket.take(KEY, START + INTERVAL - 1).allowed()).isFalse();
        assertThat(bucket.take(KEY, START + 2 * INTERVAL).remaining()).isEqualTo(1);
        assertThat(bucket.take(KEY, START + 100 * INTERVAL).remaining()).isEqualTo(CAPACITY - 1);
    }

    @Test
    @Controle
    @DisplayName("a recarga parcial conta a partir do último intervalo completo")
    void refillKeepsPartialInterval() {
        exhaust();

        assertThat(bucket.take(KEY, START + INTERVAL + 4).retry()).isEqualTo(6);
    }

    @Test
    @Controle
    @DisplayName("refund devolve o token gasto")
    void refundReturnsToken() {
        exhaust();
        bucket.refund(KEY);

        assertThat(bucket.take(KEY, START).exceeded()).isFalse();
        assertThat(bucket.take(KEY, START).exceeded()).isTrue();
    }

    @Test
    @Controle
    @DisplayName("refund não passa da capacidade nem cria bucket para chave desconhecida")
    void refundIsCappedAndIgnoresUnknownKeys() {
        bucket.take(KEY, START);
        bucket.refund(KEY);
        bucket.refund(KEY);
        bucket.refund("ip:10.0.0.2");

        assertThat(bucket.take(KEY, START).remaining()).isEqualTo(CAPACITY - 1);
        assertThat(bucket.take("ip:10.0.0.2", START).remaining()).isEqualTo(CAPACITY - 1);
    }

    @Test
    @Controle
    @DisplayName("chaves diferentes têm buckets independentes")
    void keysAreIndependent() {
        exhaust();

        assertThat(bucket.take("ip:10.0.0.2", START).allowed()).isTrue();
    }

    private void exhaust() {
        for (int i = 0; i < CAPACITY; i++) {
            bucket.take(KEY, START);
        }
    }
}
