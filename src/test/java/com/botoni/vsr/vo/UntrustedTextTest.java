package com.botoni.vsr.vo;

import com.botoni.vsr.database.enums.DevicePlatform;
import com.botoni.vsr.dto.request.DeviceRequest;
import com.botoni.vsr.support.Brecha;
import com.botoni.vsr.support.Controle;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Texto livre vindo do cliente")
class UntrustedTextTest {

    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = FACTORY.getValidator();

    @AfterAll
    static void close() {
        FACTORY.close();
    }

    @Brecha
    @ParameterizedTest(name = "nome aceita: {0}")
    @DisplayName("nome aceita HTML, caracteres de controle e de inversão de direção")
    @ValueSource(strings = {
            "<script>alert(1)</script>",
            "Ana\u0000Maria",
            "Ana‮airam",
            "Ana​Maria",
            "Ana\r\nBcc: x@y.com"
    })
    void nameAcceptsUnsafeCharacters(String value) {
        assertThat(Name.of(value).value()).isEqualTo(value.trim());
    }

    @Controle
    @ParameterizedTest(name = "fabricante/modelo recusam: {0}")
    @DisplayName("fabricante e modelo do dispositivo recusam HTML e caracteres de controle")
    @ValueSource(strings = {
            "<img src=x onerror=alert(1)>",
            "Sam\u0000sung",
            "Sam‮gnus"
    })
    void deviceRejectsUnsafeCharacters(String value) {
        DeviceRequest device = new DeviceRequest(UUID.randomUUID(), DevicePlatform.ANDROID, value, value, "14");

        assertThat(VALIDATOR.validate(device)).hasSize(2);
    }

    @Controle
    @ParameterizedTest(name = "modelo aceita: {0}")
    @DisplayName("modelos reais continuam aceitos")
    @ValueSource(strings = {"SM-S911B", "iPhone15,2", "Pixel 8 Pro (GKWS6)", "Moto g(84) 5G"})
    void deviceAcceptsRealModels(String value) {
        DeviceRequest device = new DeviceRequest(UUID.randomUUID(), DevicePlatform.ANDROID, "Samsung", value, "14");

        assertThat(VALIDATOR.validate(device)).isEmpty();
    }
}
