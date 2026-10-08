package com.botoni.vsr.controller;

import com.botoni.vsr.service.LoginService;
import com.botoni.vsr.service.RegisterService;
import com.botoni.vsr.support.Brecha;
import com.botoni.vsr.support.Controle;
import com.botoni.vsr.support.Requests;
import com.botoni.vsr.support.WebSecurityTest;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.SQLException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebSecurityTest
@DisplayName("Cadastro e login")
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RegisterService registerService;

    @Autowired
    private LoginService loginService;

    @Test
    @Controle
    @DisplayName("senha fraca é barrada pelo VO antes de chegar ao serviço")
    void weakPasswordIsRejected() throws Exception {
        mockMvc.perform(Requests.register("abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PasswordProblem.TOO_SHORT"));

        verify(registerService, never()).register(any(), any());
    }

    @Test
    @Controle
    @DisplayName("nome só com espaços é barrado pelo VO antes de chegar ao serviço")
    void blankNameIsRejected() throws Exception {
        mockMvc.perform(Requests.registerRaw(Requests.loginBody().replace("{", "{\"name\":\"   \",\"cpf\":\"529.982.247-25\",")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("NameProblem.MISSING"));

        verify(registerService, never()).register(any(), any());
    }

    @Test
    @Controle
    @DisplayName("JSON malformado responde 400 genérico, sem detalhes internos")
    void malformedBodyIsRejected() throws Exception {
        mockMvc.perform(Requests.registerRaw("{\"name\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ValidationProblem.UNREADABLE_BODY"));

        verifyNoInteractions(registerService);
    }

    @Controle
    @ParameterizedTest(name = "{0} já cadastrado responde a mesma recusa genérica")
    @CsvSource(delimiter = '|', value = {
            "e-mail | uq_usuario_email",
            "CPF    | uq_pessoa_fisica_cpf"
    })
    void registerDoesNotRevealWhichDataExists(String data, String constraint) throws Exception {
        when(registerService.register(any(), any())).thenThrow(violation(constraint));

        mockMvc.perform(Requests.register())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RegisterProblem.UNAVAILABLE"))
                .andExpect(jsonPath("$.detail").value("Não foi possível concluir o cadastro com os dados informados."));
    }

    @Brecha
    @Test
    @DisplayName("o 409 ainda revela que algum dado do cadastro já existe")
    void registerStillRevealsThatSomethingExists() throws Exception {
        when(registerService.register(any(), any())).thenThrow(violation("uq_usuario_email"));

        mockMvc.perform(Requests.register()).andExpect(status().isConflict());
    }

    @Test
    @Controle
    @DisplayName("outras violações de unicidade continuam com a mensagem específica")
    void otherConstraintsKeepSpecificMessage() throws Exception {
        when(registerService.register(any(), any())).thenThrow(violation("uq_dispositivo_usuario_identificador"));

        mockMvc.perform(Requests.register())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("UniqueConstraint.UQ_DISPOSITIVO_USUARIO_IDENTIFICADOR"));
    }

    @Test
    @Controle
    @DisplayName("endereço remoto que não é IP é recusado com 400, sem consulta DNS")
    void nonLiteralRemoteAddressIsRejected() throws Exception {
        mockMvc.perform(Requests.login().with(Requests.from("atacante.invalid")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("RequestProblem.INVALID_ADDRESS"));

        verify(loginService, never()).login(any(), any());
    }

    private static DataIntegrityViolationException violation(String constraint) {
        return new DataIntegrityViolationException(constraint,
                new ConstraintViolationException(constraint, new SQLException(), constraint));
    }
}
