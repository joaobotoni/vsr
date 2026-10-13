package com.botoni.vsr.exception.handler;

import com.botoni.vsr.service.LoginService;
import com.botoni.vsr.support.Controle;
import com.botoni.vsr.support.Requests;
import com.botoni.vsr.support.WebSecurityTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebSecurityTest
@ExtendWith(OutputCaptureExtension.class)
@DisplayName("Falha de login pela API")
class BadCredentialsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LoginService loginService;

    @Test
    @Controle
    @DisplayName("senha errada responde o 401 genérico de credenciais e registra o IP no log")
    void badCredentialsRespondGenericUnauthorized(CapturedOutput output) throws Exception {
        when(loginService.login(any(), any())).thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(Requests.login())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("SecurityProblem.BAD_CREDENTIALS"))
                .andExpect(jsonPath("$.detail").value("E-mail ou senha incorretos."));

        assertThat(output.getOut()).contains("Falha de login: ip=");
    }
}
