package com.botoni.vsr.dto.request;

import com.botoni.vsr.database.enums.DevicePlatform;
import com.botoni.vsr.support.Controle;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Dados do dispositivo")
class DeviceRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @Controle
    @DisplayName("espaços nas pontas são removidos antes de chegar ao banco")
    void surroundingSpacesAreTrimmed() {
        DeviceRequest request = new DeviceRequest(UUID.randomUUID(), DevicePlatform.ANDROID, " Samsung ", "  S23", "14 ");

        assertThat(request.manufacturer()).isEqualTo("Samsung");
        assertThat(request.model()).isEqualTo("S23");
        assertThat(request.osVersion()).isEqualTo("14");
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    @Controle
    @DisplayName("texto só com espaços continua sendo recusado na validação")
    void blankAfterTrimIsRejected() {
        DeviceRequest request = new DeviceRequest(UUID.randomUUID(), DevicePlatform.ANDROID, "   ", "S23", "14");

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("manufacturer");
    }
}
