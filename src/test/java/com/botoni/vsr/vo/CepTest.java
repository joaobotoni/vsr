package com.botoni.vsr.vo;

import com.botoni.vsr.exception.custom.CepException;
import com.botoni.vsr.exception.enums.problem.CepProblem;
import com.botoni.vsr.support.Controle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CEP")
class CepTest {

    @Controle
    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"01310100", "01310-100", "01.310-100", " 01310 100 "})
    @DisplayName("aceita o CEP com ou sem máscara e guarda só os dígitos")
    void maskIsRemoved(String value) {
        assertThat(Cep.of(value).value()).isEqualTo("01310100");
    }

    @Controle
    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "'', MISSING",
            "1234567, FORMAT",
            "123456789, FORMAT",
            "0131010a, FORMAT",
            "00000-000, ZEROS"
    })
    @DisplayName("recusa CEP vazio, com tamanho errado, com letras ou zerado")
    void invalidCepIsRejected(String value, CepProblem problem) {
        assertThatThrownBy(() -> Cep.of(value))
                .isInstanceOf(CepException.class)
                .extracting("problem").isEqualTo(problem);
    }

    @Test
    @Controle
    @DisplayName("recusa CEP nulo")
    void nullCepIsRejected() {
        assertThatThrownBy(() -> Cep.of(null))
                .isInstanceOf(CepException.class)
                .extracting("problem").isEqualTo(CepProblem.MISSING);
    }
}
