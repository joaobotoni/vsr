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
    @DisplayName("JSON malformado responde 400 genérico, sem detalhes internos")
    void malformedBodyIsRejected() throws Exception {
        mockMvc.perform(Requests.registerRaw("{\"name\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ValidationProblem.UNREADABLE_BODY"));

        verifyNoInteractions(registerService);
    }

    @Brecha
    @ParameterizedTest(name = "cadastro revela {0} já cadastrado")
    @CsvSource(delimiter = '|', value = {
            "e-mail | uq_usuario_email     | Já existe um usuário cadastrado com o e-mail informado.",
            "CPF    | uq_pessoa_fisica_cpf | Já existe um cadastro com o CPF informado."
    })
    void registerRevealsExistingData(String data, String constraint, String message) throws Exception {
        when(registerService.register(any(), any())).thenThrow(violation(constraint));

        mockMvc.perform(Requests.register())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(message));
    }

    @Test
    @Controle
    @DisplayName("endereço remoto que não é IP é recusado com 400, sem consulta DNS")
    void nonLiteralRemoteAddressIsRejected() throws Exception {
        mockMvc.perform(Requests.login().with(Requests.from("atacante.invalid"))).andExpect(status().isBadRequest());

        verify(loginService, never()).login(any(), any());
    }

    private static DataIntegrityViolationException violation(String constraint) {
        return new DataIntegrityViolationException(constraint,
                new ConstraintViolationException(constraint, new SQLException(), constraint));
    }
}
