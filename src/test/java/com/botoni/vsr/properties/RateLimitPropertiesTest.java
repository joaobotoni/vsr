package com.botoni.vsr.properties;

import com.botoni.vsr.exception.custom.LimitException;
import com.botoni.vsr.exception.enums.problem.LimitProblem;
import com.botoni.vsr.ratelimit.Limit;
import com.botoni.vsr.support.Controle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Limites de tentativas da conta")
class RateLimitPropertiesTest {

    private static final int ACCOUNT = 10;

    @Controle
    @ParameterizedTest(name = "teto {0} com limite por rede " + ACCOUNT + " impede a aplicação de subir")
    @DisplayName("teto da conta menor ou igual ao limite por conta e rede é recusado: a separação por rede perderia o efeito")
    @ValueSource(ints = {1, 5, ACCOUNT})
    void ceilingAtOrBelowAccountIsRejected(int ceiling) {
        assertThatThrownBy(() -> properties(ceiling))
                .isInstanceOf(LimitException.class)
                .extracting("problem").isEqualTo(LimitProblem.CEILING_TOO_LOW);
    }

    @Test
    @Controle
    @DisplayName("teto da conta maior que o limite por conta e rede é aceito")
    void ceilingAboveAccountIsAccepted() {
        assertThat(properties(ACCOUNT + 1).ceiling().capacity()).isEqualTo(ACCOUNT + 1);
    }

    private static RateLimitProperties properties(int ceiling) {
        Limit any = limit(100);
        return new RateLimitProperties(any, any, any, any, limit(ACCOUNT), limit(ceiling), any);
    }

    private static Limit limit(int capacity) {
        return new Limit(capacity, 1, Duration.ofMinutes(1));
    }
}
