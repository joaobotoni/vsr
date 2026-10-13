package com.botoni.vsr.vo;

import com.botoni.vsr.support.Brecha;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("Senhas fracas")
class WeakPasswordTest {

    @Brecha
    @ParameterizedTest(name = "aceita \"{0}\"")
    @DisplayName("senhas triviais ou vazadas com 12+ caracteres são aceitas")
    @ValueSource(strings = {
            "123456789012",
            "aaaaaaaaaaaa",
            "qwertyuiopas",
            "password1234",
            "ana@vsr.com1",
            "            a"
    })
    void trivialPasswordsAreAccepted(String value) {
        assertThatCode(() -> Password.of(value)).doesNotThrowAnyException();
    }
}
