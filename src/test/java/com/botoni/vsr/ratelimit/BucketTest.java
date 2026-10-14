package com.botoni.vsr.ratelimit;

import com.botoni.vsr.support.Controle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Transições do bucket")
class BucketTest {

    private static final double START = 1_000;
    private static final double INTERVAL = 10;
    private static final Limit LIMIT = new Limit(3, 1, Duration.ofSeconds((long) INTERVAL));

    @Nested
    @DisplayName("criação")
    class Full {

        @Test
        @Controle
        @DisplayName("nasce cheio, com o refill marcado no instante da criação e sem consumo")
        void startsFull() {
            assertThat(Bucket.full(LIMIT, START)).isEqualTo(new Bucket(3, START, false));
        }
    }

    @Nested
    @DisplayName("reabastecimento")
    class Refill {

        @ParameterizedTest(name = "{0}s depois → {1} tokens, último refill em +{2}s")
        @Controle
        @DisplayName("só intervalos completos contam, e o último refill avança em múltiplos do intervalo")
        @CsvSource({
                "0,     0, 0",
                "9.999, 0, 0",
                "10,    1, 10",
                "19.9,  1, 10",
                "25,    2, 20",
                "30,    3, 30",
                "1000,  3, 1000"
        })
        void refillsWholeIntervals(double after, double tokens, double refilledAfter) {
            Bucket refilled = new Bucket(0, START, false).refill(LIMIT, START + after);

            assertThat(refilled.tokens()).isEqualTo(tokens);
            assertThat(refilled.moment()).isEqualTo(START + refilledAfter);
        }

        @Test
        @Controle
        @DisplayName("antes de um intervalo completo devolve o mesmo bucket, intacto")
        void noIntervalKeepsSameBucket() {
            Bucket bucket = new Bucket(1, START, true);

            assertThat(bucket.refill(LIMIT, START + INTERVAL - 0.001)).isSameAs(bucket);
        }

        @Test
        @Controle
        @DisplayName("nunca passa da capacidade, mesmo partindo de um bucket já cheio")
        void neverExceedsCapacity() {
            assertThat(Bucket.full(LIMIT, START).refill(LIMIT, START + 50 * INTERVAL).tokens()).isEqualTo(3);
        }

        @Test
        @Controle
        @DisplayName("ao reabastecer, o resultado do consumo anterior é descartado")
        void refillClearsAllowed() {
            assertThat(new Bucket(0, START, true).refill(LIMIT, START + INTERVAL).allowed()).isFalse();
        }

        @Test
        @Controle
        @DisplayName("taxa fracionária acumula até completar um token")
        void fractionalRateAccumulates() {
            Limit slow = new Limit(2, 0.5, Duration.ofSeconds(10));
            Bucket empty = new Bucket(0, START, false);

            assertThat(empty.refill(slow, START + 10).tokens()).isEqualTo(0.5);
            assertThat(empty.refill(slow, START + 10).consume().allowed()).isFalse();
            assertThat(empty.refill(slow, START + 20).consume().allowed()).isTrue();
        }

        @Test
        @Controle
        @DisplayName("intervalo em milissegundos é respeitado")
        void millisecondInterval() {
            Limit fast = new Limit(4, 1, Duration.ofMillis(1500));
            Bucket refilled = new Bucket(0, START, false).refill(fast, START + 4);

            assertThat(refilled.tokens()).isEqualTo(2);
            assertThat(refilled.moment()).isEqualTo(START + 3);
        }
    }

    @Nested
    @DisplayName("retirada")
    class Take {

        @ParameterizedTest(name = "{0} tokens → permitido, sobram {1}")
        @Controle
        @DisplayName("com pelo menos um token, retira um e marca como permitido")
        @CsvSource({"3, 2", "1, 0", "1.5, 0.5", "2.25, 1.25"})
        void grantsWithToken(double tokens, double left) {
            assertThat(new Bucket(tokens, START, false).consume()).isEqualTo(new Bucket(left, START, true));
        }

        @ParameterizedTest(name = "{0} tokens → recusado, saldo intacto")
        @Controle
        @DisplayName("com menos de um token, recusa sem gastar nada")
        @CsvSource({"0", "0.5", "0.999"})
        void deniesWithoutToken(double tokens) {
            assertThat(new Bucket(tokens, START, true).consume()).isEqualTo(new Bucket(tokens, START, false));
        }

        @Test
        @Controle
        @DisplayName("retiradas sucessivas esgotam exatamente a capacidade")
        void successiveConsumesExhaustCapacity() {
            Bucket bucket = Bucket.full(LIMIT, START);
            int granted = 0;
            for (int i = 0; i < 10; i++) {
                bucket = bucket.consume();
                granted += bucket.allowed() ? 1 : 0;
            }

            assertThat(granted).isEqualTo(3);
            assertThat(bucket.tokens()).isZero();
        }
    }

    @Nested
    @DisplayName("devolução")
    class Restore {

        @Test
        @Controle
        @DisplayName("devolve um token sem mexer no último refill nem no resultado do consumo")
        void refundsOneToken() {
            assertThat(new Bucket(0, START, true).refund(LIMIT)).isEqualTo(new Bucket(1, START, true));
            assertThat(new Bucket(0.5, START, false).refund(LIMIT)).isEqualTo(new Bucket(1.5, START, false));
        }

        @ParameterizedTest(name = "{0} tokens → {1}")
        @Controle
        @DisplayName("nunca passa da capacidade")
        @CsvSource({"3, 3", "2.5, 3", "2, 3"})
        void cappedAtCapacity(double tokens, double restored) {
            assertThat(new Bucket(tokens, START, false).refund(LIMIT).tokens()).isEqualTo(restored);
        }

        @Test
        @Controle
        @DisplayName("retirar e devolver volta ao saldo original")
        void consumeThenRefundIsNeutral() {
            Bucket bucket = new Bucket(2, START, false);

            assertThat(bucket.consume().refund(LIMIT).tokens()).isEqualTo(bucket.tokens());
        }
    }

    @Nested
    @DisplayName("leitura")
    class Reading {

        @ParameterizedTest(name = "{0} tokens → restam {1}")
        @Controle
        @DisplayName("tokens restantes arredondam para baixo")
        @CsvSource({"0, 0", "0.999, 0", "1, 1", "2.7, 2", "3, 3"})
        void remainingFloors(double tokens, double remaining) {
            assertThat(new Bucket(tokens, START, false).remaining()).isEqualTo(remaining);
        }

        @ParameterizedTest(name = "{0}s depois do refill → decorrido {0}s, reset em {1}s")
        @Controle
        @DisplayName("o reset é o tempo que falta para o próximo refill")
        @CsvSource({"0, 10", "4, 6", "9.5, 0.5", "10, 0"})
        void resetIsTimeToNextRefill(double after, double reset) {
            Bucket bucket = new Bucket(0, START, false);

            assertThat(bucket.elapsed(START + after)).isEqualTo(after);
            assertThat(bucket.reset(LIMIT, START + after)).isEqualTo(reset);
        }
    }
}
