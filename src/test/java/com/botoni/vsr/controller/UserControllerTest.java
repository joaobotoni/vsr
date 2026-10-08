package com.botoni.vsr.controller;

import com.botoni.vsr.service.ChangePasswordService;
import com.botoni.vsr.support.Controle;
import com.botoni.vsr.support.Requests;
import com.botoni.vsr.support.Tokens;
import com.botoni.vsr.support.Users;
import com.botoni.vsr.support.WebSecurityTest;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.SQLException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebSecurityTest
@DisplayName("Troca de senha pela API")
class UserControllerTest {

    private static final String CONSTRAINT = "rn_senha_alterada";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ChangePasswordService changePasswordService;

    @Test
    @Controle
    @DisplayName("troca de senha válida responde 204 para o usuário e a sessão do token")
    void changeRespondsNoContent() throws Exception {
        mockMvc.perform(Requests.changePassword(Tokens.valid())).andExpect(status().isNoContent());

        verify(changePasswordService).change(eq(Users.UUID), eq(Tokens.SESSION), any(), any());
    }

    @Test
    @Controle
    @DisplayName("troca concorrente detectada no banco responde 409")
    void concurrentChangeRespondsConflict() throws Exception {
        doThrow(new DataIntegrityViolationException(CONSTRAINT,
                new ConstraintViolationException(CONSTRAINT, new SQLException(), CONSTRAINT)))
                .when(changePasswordService).change(any(), any(), any(), any());

        mockMvc.perform(Requests.changePassword(Tokens.valid()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RuleConstraint.RN_SENHA_ALTERADA"));
    }
}
