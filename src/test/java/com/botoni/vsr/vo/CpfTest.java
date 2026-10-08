package com.botoni.vsr.vo;

import com.botoni.vsr.exception.custom.CpfException;
import com.botoni.vsr.exception.enums.problem.CpfProblem;
import com.botoni.vsr.support.Controle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Regras de CPF")
class CpfTest {

    @Controle
    @ParameterizedTest(name = "\"{0}\" é aceito e guardado só com dígitos")
    @CsvSource({"529.982.247-25", "52998224725", "' 529 982 247 25 '"})
    void validCpfIsNormalized(String value) {
        assertThat(Cpf.of(value).value()).isEqualTo("52998224725");
    }

    @Controle
    @ParameterizedTest(name = "{0} é recusado com {2}")
    @CsvSource(delimiter = '|', value = {
            "vazio                       | ''              | MISSING",
            "só máscara                  | '..-'           | MISSING",
            "curto                       | 1234            | LENGTH",
            "longo                       | 529982247250    | LENGTH",
            "com letra                   | 5299822472a     | CHARACTERS",
            "todos os dígitos iguais     | 111.111.111-11  | REPEATED_DIGITS",
            "dígito verificador errado   | 529.982.247-24  | CHECK_DIGITS"
    })
    void invalidCpfIsRejected(String description, String value, CpfProblem problem) {
        assertThatThrownBy(() -> Cpf.of(value))
                .isInstanceOf(CpfException.class)
                .extracting("problem").isEqualTo(problem);
    }

    @Test
    @Controle
    @DisplayName("CPF nulo é recusado")
    void nullCpfIsRejected() {
        assertThatThrownBy(() -> Cpf.of(null))
                .isInstanceOf(CpfException.class)
                .extracting("problem").isEqualTo(CpfProblem.MISSING);
    }
}
