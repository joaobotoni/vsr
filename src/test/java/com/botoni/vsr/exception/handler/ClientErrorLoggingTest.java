package com.botoni.vsr.exception.handler;

import com.botoni.vsr.support.Controle;
import com.botoni.vsr.support.Ips;
import com.botoni.vsr.support.Requests;
import com.botoni.vsr.support.WebSecurityTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebSecurityTest
@ExtendWith(OutputCaptureExtension.class)
@DisplayName("Log de erros causados pelo cliente")
class ClientErrorLoggingTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Controle
    @DisplayName("Content-Type errado (erro do cliente, 415) vira uma linha WARN, sem stack trace")
    void clientErrorIsLoggedAsWarning(CapturedOutput output) throws Exception {
        mockMvc.perform(post("/api/1/auth/login").with(Requests.from(Ips.next()))
                        .contentType(MediaType.TEXT_PLAIN).content("x"))
                .andExpect(status().isUnsupportedMediaType());

        assertThat(output.getOut())
                .contains("WARN")
                .contains("Requisição recusada: HttpMediaTypeNotSupportedException")
                .doesNotContain("Erro inesperado ao processar a requisição")
                .doesNotContain("\tat org.springframework");
    }
}
