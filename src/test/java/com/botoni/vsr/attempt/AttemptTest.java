package com.botoni.vsr.attempt;

import com.botoni.vsr.support.Controle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Tentativa")
class AttemptTest {

    private final Attempt attempt = Attempt.of("login:ana@vsr.com", IllegalArgumentException.class);

    @Test
    @Controle
    @DisplayName("a exceção de falha e suas subclasses são falha")
    void failureAndSubclassesFail() {
        assertThat(attempt.fails(new IllegalArgumentException())).isTrue();
        assertThat(attempt.fails(new NumberFormatException())).isTrue();
    }

    @Test
    @Controle
    @DisplayName("qualquer outra exceção não é falha")
    void otherExceptionsDoNotFail() {
        assertThat(attempt.fails(new IllegalStateException())).isFalse();
    }

    @Controle
    @NullSource
    @ParameterizedTest(name = "teto \"{0}\" é recusado")
    @DisplayName("tentativa com teto exige a chave de teto: teto vazio parece ter teto sem ter")
    @ValueSource(strings = {"", "   "})
    void emptyCeilingIsRejected(String ceiling) {
        assertThatThrownBy(() -> Attempt.ceiled("login:ana@vsr.com:network:1", ceiling, IllegalArgumentException.class))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @Controle
    @DisplayName("só a tentativa montada com teto é limitada pelo teto")
    void onlyCeiledAttemptIsLimited() {
        assertThat(attempt.limited()).isFalse();
        assertThat(Attempt.ceiled("login:ana@vsr.com:network:1", "login:ana@vsr.com", IllegalArgumentException.class).limited()).isTrue();
    }
}
