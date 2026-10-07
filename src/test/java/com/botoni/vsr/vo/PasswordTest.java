package com.botoni.vsr.vo;

import com.botoni.vsr.exception.custom.PasswordException;
import com.botoni.vsr.exception.enums.problem.PasswordProblem;
import com.botoni.vsr.support.Controle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Regras de senha")
class PasswordTest {

    @Test
    @Controle
    @DisplayName("frase com espaços e só letras minúsculas é aceita, sem ser aparada")
    void passphraseIsAccepted() {
        assertThat(Password.of(" minha casa fica perto do rio ").value()).isEqualTo(" minha casa fica perto do rio ");
    }

    @Test
    @Controle
    @DisplayName("senha com menos de 12 caracteres é recusada")
    void shortPasswordIsRejected() {
        assertThatThrownBy(() -> Password.of("Curta@12345"))
                .isInstanceOf(PasswordException.class)
                .extracting("problem").isEqualTo(PasswordProblem.TOO_SHORT);
    }

    @Test
    @Controle
    @DisplayName("aceita até 128 caracteres e recusa a partir de 129")
    void maximumLengthIsEnforced() {
        assertThat(Password.of("a".repeat(128)).value()).hasSize(128);
        assertThatThrownBy(() -> Password.of("a".repeat(129)))
                .isInstanceOf(PasswordException.class)
                .extracting("problem").isEqualTo(PasswordProblem.TOO_LONG);
    }

    @Test
    @Controle
    @DisplayName("acentos e emojis contam como um caractere cada")
    void lengthCountsCharactersNotUnits() {
        assertThatThrownBy(() -> Password.of("🔒".repeat(11)))
                .extracting("problem").isEqualTo(PasswordProblem.TOO_SHORT);
        assertThat(Password.of("🔒".repeat(12)).value()).isNotBlank();
    }

    @Test
    @Controle
    @DisplayName("senha só com espaços é recusada")
    void blankPasswordIsRejected() {
        assertThatThrownBy(() -> Password.of(" ".repeat(20)))
                .extracting("problem").isEqualTo(PasswordProblem.MISSING);
    }

    @Test
    @Controle
    @DisplayName("o valor nunca aparece no toString")
    void valueIsHiddenInToString() {
        assertThat(Password.of("senha segura 123")).hasToString("****");
    }
}
