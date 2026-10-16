package com.botoni.vsr.vo;

import com.botoni.vsr.exception.custom.PasswordException;
import com.botoni.vsr.exception.enums.problem.PasswordProblem;
import com.botoni.vsr.support.Controle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Senha nova (cadastro e troca)")
class NewPasswordTest {

    @Test
    @Controle
    @DisplayName("senha com maiúscula, minúscula, dígito e caractere especial é aceita e vira a senha guardada")
    void completePasswordIsAccepted() {
        NewPassword password = NewPassword.of("Senha@Segura123");

        assertThat(password.value()).isEqualTo("Senha@Segura123");
        assertThat(password.password()).isEqualTo(Password.of("Senha@Segura123"));
    }

    @Controle
    @ParameterizedTest(name = "\"{0}\" é recusada por {1}")
    @DisplayName("senha nova que não cumpre uma das regras é recusada pelo motivo certo")
    @CsvSource(delimiter = '|', value = {
            "Ab@1234          | TOO_SHORT",
            "senha@segura1    | MISSING_UPPERCASE",
            "SENHA@SEGURA1    | MISSING_LOWERCASE",
            "Senha@Segura     | MISSING_DIGIT",
            "SenhaSegura1     | MISSING_SPECIAL_CHARACTER",
            "'Senha @Segura1' | CONTAINS_WHITESPACE"
    })
    void incompletePasswordIsRejected(String value, PasswordProblem problem) {
        assertThatThrownBy(() -> NewPassword.of(value))
                .isInstanceOf(PasswordException.class)
                .extracting("problem").isEqualTo(problem);
    }

    @Test
    @Controle
    @DisplayName("o limite de 72 bytes e a senha vazia vêm da senha guardada")
    void storedPasswordLimitsApply() {
        assertThatThrownBy(() -> NewPassword.of("Aa1@" + "a".repeat(69)))
                .extracting("problem").isEqualTo(PasswordProblem.TOO_LONG);
        assertThatThrownBy(() -> NewPassword.of("   "))
                .extracting("problem").isEqualTo(PasswordProblem.MISSING);
    }

    @Test
    @Controle
    @DisplayName("o valor nunca aparece no toString")
    void valueIsHiddenInToString() {
        assertThat(NewPassword.of("Senha@Segura123")).hasToString("****");
    }
}
