package com.botoni.vsr.vo;

import com.botoni.vsr.exception.custom.EmailException;
import com.botoni.vsr.exception.enums.problem.EmailProblem;
import com.botoni.vsr.support.Controle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Regras de e-mail")
class EmailTest {

    @Test
    @Controle
    @DisplayName("e-mail é aparado e guardado em minúsculas")
    void emailIsNormalized() {
        assertThat(Email.of("  Ana.Silva+vsr@VSR.com.BR ").value()).isEqualTo("ana.silva+vsr@vsr.com.br");
    }

    @Test
    @Controle
    @DisplayName("maiúsculas não criam um e-mail diferente")
    void caseDoesNotCreateAnotherEmail() {
        assertThat(Email.of("ANA@VSR.COM")).isEqualTo(Email.of("ana@vsr.com"));
    }

    @Controle
    @ParameterizedTest(name = "{0} é recusado com {2}")
    @CsvSource(delimiter = '|', value = {
            "vazio                    | ''              | MISSING",
            "só espaços               | '   '           | MISSING",
            "com espaço no meio       | ana silva@vsr.com | CONTAINS_WHITESPACE",
            "sem @                    | anavsr.com      | MISSING_AT_SIGN",
            "com dois @               | ana@x@vsr.com   | MULTIPLE_AT_SIGNS",
            "caractere inválido local | ana!@vsr.com    | INVALID_LOCAL_PART",
            "domínio sem ponto        | ana@vsr         | INVALID_DOMAIN",
            "domínio com _            | ana@v_sr.com    | INVALID_DOMAIN",
            "extensão de uma letra    | ana@vsr.c       | INVALID_TOP_LEVEL_DOMAIN",
            "extensão com número      | ana@vsr.c0m     | INVALID_TOP_LEVEL_DOMAIN"
    })
    void invalidEmailIsRejected(String description, String value, EmailProblem problem) {
        assertThatThrownBy(() -> Email.of(value))
                .isInstanceOf(EmailException.class)
                .extracting("problem").isEqualTo(problem);
    }

    @Test
    @Controle
    @DisplayName("aceita até 64 caracteres antes do @ e recusa a partir de 65")
    void localPartLengthIsEnforced() {
        assertThat(Email.of("a".repeat(64) + "@vsr.com").value()).startsWith("a".repeat(64));
        assertThatThrownBy(() -> Email.of("a".repeat(65) + "@vsr.com"))
                .extracting("problem").isEqualTo(EmailProblem.LOCAL_PART_TOO_LONG);
    }

    @Test
    @Controle
    @DisplayName("aceita até 254 caracteres e recusa a partir de 255")
    void totalLengthIsEnforced() {
        assertThat(Email.of(emailOf(254)).value()).hasSize(254);
        assertThatThrownBy(() -> Email.of(emailOf(255)))
                .extracting("problem").isEqualTo(EmailProblem.TOO_LONG);
    }

    private static String emailOf(int length) {
        String suffix = "@" + "b".repeat(length - 15) + ".com";
        return "a".repeat(length - suffix.length()) + suffix;
    }

    @Test
    @Controle
    @DisplayName("e-mail nulo é recusado")
    void nullEmailIsRejected() {
        assertThatThrownBy(() -> Email.of(null))
                .isInstanceOf(EmailException.class)
                .extracting("problem").isEqualTo(EmailProblem.MISSING);
    }
}
