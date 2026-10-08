package com.botoni.vsr.vo;

import com.botoni.vsr.exception.custom.CnpjException;
import com.botoni.vsr.exception.enums.problem.CnpjProblem;
import com.botoni.vsr.support.Controle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Regras de CNPJ")
class CnpjTest {

    @Controle
    @ParameterizedTest(name = "\"{0}\" é aceito como {1}")
    @CsvSource(delimiter = '|', value = {
            "11.222.333/0001-81  | 11222333000181",
            "11222333000181      | 11222333000181",
            "12.ABC.345/01DE-35  | 12ABC34501DE35",
            "12.abc.345/01de-35  | 12ABC34501DE35"
    })
    void validCnpjIsNormalized(String value, String expected) {
        assertThat(Cnpj.of(value).value()).isEqualTo(expected);
    }

    @Controle
    @ParameterizedTest(name = "{0} é recusado com {2}")
    @CsvSource(delimiter = '|', value = {
            "vazio                          | ''                  | MISSING",
            "curto                          | 123                 | LENGTH",
            "com caractere inválido         | 11222333000!81      | CHARACTERS",
            "dígitos verificadores com letra | 112223330001AB     | NON_NUMERIC_CHECK_DIGITS",
            "todos os caracteres iguais     | 00.000.000/0000-00  | REPEATED_CHARACTERS",
            "dígito verificador errado      | 11.222.333/0001-82  | CHECK_DIGITS"
    })
    void invalidCnpjIsRejected(String description, String value, CnpjProblem problem) {
        assertThatThrownBy(() -> Cnpj.of(value))
                .isInstanceOf(CnpjException.class)
                .extracting("problem").isEqualTo(problem);
    }

    @Test
    @Controle
    @DisplayName("CNPJ nulo é recusado")
    void nullCnpjIsRejected() {
        assertThatThrownBy(() -> Cnpj.of(null))
                .isInstanceOf(CnpjException.class)
                .extracting("problem").isEqualTo(CnpjProblem.MISSING);
    }
}
