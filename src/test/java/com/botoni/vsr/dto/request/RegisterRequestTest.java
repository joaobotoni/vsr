package com.botoni.vsr.dto.request;

import com.botoni.vsr.database.enums.DevicePlatform;
import com.botoni.vsr.support.Controle;
import com.botoni.vsr.vo.Cpf;
import com.botoni.vsr.vo.Email;
import com.botoni.vsr.vo.Name;
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
    @DisplayName("cadastro com todos os dados é válido")
    void completeRequestIsValid() {
        assertThat(validator.validate(request(Name.of("Ana Maria")))).isEmpty();
    }

    @Test
    @Controle
    @DisplayName("nome ausente é recusado na validação")
    void missingNameIsRejected() {
        assertThat(validator.validate(request(null)))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("name");
    }

    private static RegisterRequest request(Name name) {
        DeviceRequest device = new DeviceRequest(UUID.randomUUID(), DevicePlatform.ANDROID, "Samsung", "S23", "14");
        return new RegisterRequest(name, Cpf.of("52998224725"), Email.of("ana@vsr.com"), Password.of("senha segura 123"), device);
    }
}
