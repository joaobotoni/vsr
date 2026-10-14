package com.botoni.vsr.ratelimit;

import com.botoni.vsr.support.Controle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Executa sequências longas e aleatórias (com semente fixa) de consumo, devolução e passagem do tempo e compara cada
 * resposta com um modelo de referência escrito de forma direta.
 */
@DisplayName("Token bucket contra o modelo de referência")
class TokenBucketModelTest {

    private static final long SEED = 20261014L;
    private static final int OPERATIONS = 50_000;
    private static final int KEYS = 12;
    private static final double MAX_STEP_SECONDS = 4;
    private static final double REFUND_CHANCE = 0.3;

    @Controle
    @ParameterizedTest(name = "capacidade {0}, taxa {1}, intervalo {2}ms")
    @DisplayName("toda resposta e todo saldo batem com o modelo em sequências aleatórias")
    @CsvSource({
            "3,  1,   10000",
            "5,  0.5, 1500",
            "1,  2,   250",
            "10, 3,   1000",
            "100, 7,  60000",
            "2,  0.25, 333"
    })
    void matchesModel(int capacity, double refillRate, long intervalMillis) {
        Limit limit = new Limit(capacity, refillRate, Duration.ofMillis(intervalMillis));
        TokenBucket bucket = new TokenBucket(limit, new ConcurrentHashMap<>());
        Model model = new Model(limit);
        Random random = new Random(SEED + capacity);
        double now = 1_000;

        for (int i = 0; i < OPERATIONS; i++) {
            now += random.nextDouble() * MAX_STEP_SECONDS * random.nextDouble();
            String key = "ip:10.0.0." + random.nextInt(KEYS);
            if (random.nextDouble() < REFUND_CHANCE) {
                bucket.refund(key);
                model.refund(key);
                continue;
            }
            Verdict verdict = bucket.take(key, now);

            assertThat(verdict).as("operação %d, chave %s, t=%s", i, key, now).isEqualTo(model.consume(key, now));
            assertThat(verdict.remaining()).isBetween(0.0, (double) capacity - 1);
            assertThat(verdict.reset()).isGreaterThan(0).isLessThanOrEqualTo(limit.interval());
        }
    }

    /** Implementação direta do algoritmo, sem decomposição, usada como oráculo. */
    private static final class Model {

        private final Limit limit;
        private final Map<String, double[]> state = new HashMap<>();

        private Model(Limit limit) {
            this.limit = limit;
        }

        Verdict consume(String key, double now) {
            double interval = limit.interval();
            double[] bucket = state.computeIfAbsent(key, k -> new double[]{limit.capacity(), now});
            double refills = Math.floor((now - bucket[1]) / interval);
            if (refills > 0) {
                bucket[0] = Math.min(limit.capacity(), bucket[0] + refills * limit.rate());
                bucket[1] = bucket[1] + refills * interval;
            }
            boolean allowed = bucket[0] >= 1;
            if (allowed) {
                bucket[0] = bucket[0] - 1;
            }
            return new Verdict(allowed, Math.floor(bucket[0]), interval - (now - bucket[1]));
        }

        void refund(String key) {
            double[] bucket = state.get(key);
            if (bucket != null) {
                bucket[0] = Math.min(limit.capacity(), bucket[0] + 1);
            }
        }
    }
}
