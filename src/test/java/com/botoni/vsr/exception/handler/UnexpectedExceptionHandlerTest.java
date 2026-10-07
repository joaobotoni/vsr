package com.botoni.vsr.exception.handler;

import com.botoni.vsr.service.UserService;
import com.botoni.vsr.support.Controle;
import com.botoni.vsr.support.Requests;
import com.botoni.vsr.support.Tokens;
import com.botoni.vsr.support.Users;
import com.botoni.vsr.support.WebSecurityTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebSecurityTest
@ExtendWith(OutputCaptureExtension.class)
@DisplayName("Erros inesperados")
class UnexpectedExceptionHandlerTest {

    private static final String INTERNAL = "detalhe-interno-do-banco";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UserService userService;

    @BeforeEach
    void failingService() {
        when(userDetailsService.loadUserByUsername(Users.EMAIL)).thenReturn(Users.principal());
        when(userService.profile(any())).thenThrow(new IllegalStateException(INTERNAL));
    }

    @Test
    @Controle
    @DisplayName("resposta 500 é genérica e não vaza a mensagem interna")
    void responseDoesNotLeakInternals() throws Exception {
        mockMvc.perform(Requests.me(Tokens.valid()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("UnexpectedProblem.UNEXPECTED_ERROR"))
                .andExpect(content().string(not(containsString(INTERNAL))));
    }

    @Test
    @Controle
    @DisplayName("a exceção é registrada no log do servidor, mas não vai para a resposta")
    void exceptionIsLogged(CapturedOutput output) throws Exception {
        mockMvc.perform(Requests.me(Tokens.valid())).andExpect(status().isInternalServerError());

        assertThat(output).contains("Erro inesperado ao processar a requisição").contains(INTERNAL);
    }
}
