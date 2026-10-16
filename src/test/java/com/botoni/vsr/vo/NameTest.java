package com.botoni.vsr.vo;

import com.botoni.vsr.exception.custom.NameException;
import com.botoni.vsr.exception.enums.problem.NameProblem;
import com.botoni.vsr.support.Controle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Regras de nome")
class NameTest {

    @Controle
    @ParameterizedTest(name = "aceita \"{0}\"")
    @DisplayName("nomes com letras, acentos e espaços são aceitos")
    @ValueSource(strings = {"Ana", "Ana Maria", "João da Conceição", "Zoë Müller", "Ñandú Ávila", "Иван Петров"})
    void lettersAndSpacesAreAccepted(String value) {
        assertThat(Name.of(value).value()).isEqualTo(value);
    }

    @Controle
    @ParameterizedTest(name = "\"{0}\" vira \"{1}\"")
    @DisplayName("espaços nas pontas somem e espaços repetidos viram um só")
    @CsvSource(delimiter = '|', value = {
            "'  Ana Maria '     | Ana Maria",
            "'Ana    Maria'     | Ana Maria",
            "'Ana \t Maria'     | Ana Maria"
    })
    void spacesAreNormalized(String value, String expected) {
        assertThat(Name.of(value).value()).isEqualTo(expected);
    }

    @Test
    @Controle
    @DisplayName("acento digitado como letra mais acento combinado é guardado na forma composta")
    void accentsAreComposed() {
        assertThat(Name.of("José").value()).isEqualTo("José");
    }

    @Controle
    @ParameterizedTest(name = "recusa \"{0}\"")
    @DisplayName("dígitos, pontuação, símbolos, HTML e caracteres invisíveis ou de inversão de direção são recusados")
    @ValueSource(strings = {
            "Ana Maria 2",
            "Ana.Maria",
            "Ana_Maria",
            "Ana@Maria",
            "Ana & Maria",
            "D'Ávila",
            "Maria-José",
            "<script>alert(1)</script>",
            "Ana\u0000Maria",
            "Ana​Maria",
            "Ana‮airam",
            "Ana\r\nBcc: x@y.com"
    })
    void otherCharactersAreRejected(String value) {
        assertThatThrownBy(() -> Name.of(value))
                .isInstanceOf(NameException.class)
                .extracting("problem").isEqualTo(NameProblem.INVALID_CHARACTERS);
    }

    @Controle
    @ParameterizedTest(name = "recusa \"{0}\"")
    @DisplayName("acento solto, palavra feita só de acentos ou mais de dois acentos na mesma letra são recusados")
    @ValueSource(strings = {
            "́",
            "Ana ́́",
            "́Ana",
            "Ana Má̂̃̄ria",
            "Z̶̵̴̷algo"
    })
    void detachedOrStackedMarksAreRejected(String value) {
        assertThatThrownBy(() -> Name.of(value))
                .isInstanceOf(NameException.class)
                .extracting("problem").isEqualTo(NameProblem.INVALID_CHARACTERS);
    }

    @Controle
    @ParameterizedTest(name = "aceita \"{0}\"")
    @DisplayName("letra com até dois acentos combinados que não têm forma composta é aceita")
    @ValueSource(strings = {"Nguyễn", "A̱na", "Ana Marį́a"})
    void lettersWithUpToTwoMarksAreAccepted(String value) {
        assertThat(Name.of(value).value()).isNotBlank();
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
