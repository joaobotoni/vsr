package com.botoni.vsr.vo;

import com.botoni.vsr.exception.custom.NameException;
import com.botoni.vsr.exception.enums.problem.NameProblem;
import com.botoni.vsr.support.Controle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Regras de nome")
class NameTest {

    @Test
    @Controle
    @DisplayName("espaços nas pontas são removidos, os do meio ficam")
    void surroundingSpacesAreTrimmed() {
        assertThat(Name.of("  Ana Maria ").value()).isEqualTo("Ana Maria");
    }

    @Controle
    @ParameterizedTest(name = "\"{0}\" é recusado como ausente")
    @ValueSource(strings = {"", "   ", "\t\n"})
    void blankNameIsRejected(String value) {
        assertThatThrownBy(() -> Name.of(value))
                .isInstanceOf(NameException.class)
                .extracting("problem").isEqualTo(NameProblem.MISSING);
    }

    @Test
    @Controle
    @DisplayName("nome nulo é recusado")
    void nullNameIsRejected() {
        assertThatThrownBy(() -> Name.of(null))
                .isInstanceOf(NameException.class)
                .extracting("problem").isEqualTo(NameProblem.MISSING);
    }

    @Test
    @Controle
    @DisplayName("aceita até 200 caracteres e recusa a partir de 201, contando acentos como um")
    void maximumLengthIsEnforced() {
        assertThat(Name.of("é".repeat(200)).value()).hasSize(200);
        assertThatThrownBy(() -> Name.of("é".repeat(201)))
                .isInstanceOf(NameException.class)
                .extracting("problem").isEqualTo(NameProblem.TOO_LONG);
    }
}
