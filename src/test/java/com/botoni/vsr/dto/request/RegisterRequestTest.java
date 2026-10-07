package com.botoni.vsr.dto.request;

import com.botoni.vsr.database.enums.DevicePlatform;
import com.botoni.vsr.support.Controle;
import com.botoni.vsr.vo.Cpf;
import com.botoni.vsr.vo.Email;
import com.botoni.vsr.vo.Password;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Dados do cadastro")
class RegisterRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @Controle
    @DisplayName("espaços nas pontas do nome são removidos antes da validação")
    void surroundingSpacesAreTrimmed() {
        RegisterRequest request = request("  Ana Maria ");

        assertThat(request.name()).isEqualTo("Ana Maria");
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    @Controle
    @DisplayName("nome só com espaços é recusado na validação")
    void blankNameIsRejected() {
        assertThat(validator.validate(request("   ")))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("name");
    }

    private static RegisterRequest request(String name) {
        DeviceRequest device = new DeviceRequest(UUID.randomUUID(), DevicePlatform.ANDROID, "Samsung", "S23", "14");
        return new RegisterRequest(name, Cpf.of("52998224725"), Email.of("ana@vsr.com"), Password.of("senha segura 123"), device);
    }
}
