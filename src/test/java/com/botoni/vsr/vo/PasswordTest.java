package com.botoni.vsr.vo;

import com.botoni.vsr.exception.custom.PasswordException;
import com.botoni.vsr.exception.enums.problem.PasswordProblem;
import com.botoni.vsr.support.Controle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Senha existente (login e senha atual)")
class PasswordTest {

    @Controle
    @ParameterizedTest(name = "aceita \"{0}\"")
    @DisplayName("senha criada sob regras antigas continua entrando: as regras de criação não valem aqui")
    @ValueSource(strings = {"minha casa fica perto do rio", "senha123", "SOMENTEMAIUSCULA", "a"})
    void existingPasswordIsAccepted(String value) {
        assertThat(Password.of(value).value()).isEqualTo(value);
    }

    @Test
    @Controle
    @DisplayName("aceita até 72 bytes e recusa a partir de 73")
    void maximumLengthIsEnforced() {
        assertThat(Password.of("a".repeat(72)).value()).hasSize(72);
        assertThatThrownBy(() -> Password.of("a".repeat(73)))
                .isInstanceOf(PasswordException.class)
                .extracting("problem").isEqualTo(PasswordProblem.TOO_LONG);
    }

    @Test
    @Controle
    @DisplayName("senha vazia ou só com espaços é recusada")
    void blankPasswordIsRejected() {
        assertThatThrownBy(() -> Password.of(" ".repeat(20)))
                .extracting("problem").isEqualTo(PasswordProblem.MISSING);
        assertThatThrownBy(() -> Password.of(null))
                .extracting("problem").isEqualTo(PasswordProblem.MISSING);
    }

    @Test
    @Controle
    @DisplayName("o valor nunca aparece no toString")
    void valueIsHiddenInToString() {
        assertThat(Password.of("Senha@Segura123")).hasToString("****");
    }
}
