package com.botoni.vsr.filter;

import com.botoni.vsr.service.LoginService;
import com.botoni.vsr.support.Controle;
import com.botoni.vsr.support.Requests;
import com.botoni.vsr.support.Tokens;
import com.botoni.vsr.support.WebSecurityTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebSecurityTest
@DisplayName("Limite de tamanho do corpo")
class RequestBodySizeLimitFilterTest {

    private static final int LIMIT = 16 * 1024;
    private static final String TRANSFER_ENCODING = "Transfer-Encoding";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LoginService loginService;

    @Test
    @Controle
    @DisplayName("corpo com exatamente o limite é aceito")
    void bodyAtLimitIsAccepted() throws Exception {
        mockMvc.perform(Requests.loginRaw(bodyOf(LIMIT))).andExpect(status().isOk());
    }

    @Test
    @Controle
    @DisplayName("corpo um byte acima do limite responde 413 sem chegar ao serviço")
    void bodyAboveLimitIsRejected() throws Exception {
        mockMvc.perform(Requests.loginRaw(bodyOf(LIMIT + 1)))
                .andExpect(status().isContentTooLarge())
                .andExpect(jsonPath("$.code").value("RequestProblem.BODY_TOO_LARGE"));

        verify(loginService, never()).login(any(), any());
    }

    @Test
    @Controle
    @DisplayName("corpo sem Content-Length (chunked) responde 411")
    void chunkedBodyIsRejected() throws Exception {
        mockMvc.perform(Requests.login().header(TRANSFER_ENCODING, "chunked"))
                .andExpect(status().isLengthRequired())
                .andExpect(jsonPath("$.code").value("RequestProblem.LENGTH_REQUIRED"));
    }

    @Test
    @Controle
    @DisplayName("Transfer-Encoding é reconhecido sem diferenciar maiúsculas")
    void chunkedIsCaseInsensitive() throws Exception {
        mockMvc.perform(Requests.login().header(TRANSFER_ENCODING, "Chunked")).andExpect(status().isLengthRequired());
    }

    @Test
    @Controle
    @DisplayName("requisição sem corpo passa pelo filtro")
    void requestWithoutBodyPasses() throws Exception {
        mockMvc.perform(Requests.me(Tokens.valid())).andExpect(status().isOk());
    }

    @Test
    @Controle
    @DisplayName("corpo grande é recusado antes da autenticação, mesmo sem token")
    void sizeIsCheckedBeforeAuthentication() throws Exception {
        mockMvc.perform(patch("/api/1/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(" ".repeat(LIMIT + 1)))
                .andExpect(status().isContentTooLarge());
    }

    @Test
    @Controle
    @DisplayName("requisição recusada por tamanho ainda consome o rate limit")
    void rejectedRequestCarriesRateLimitHeaders() throws Exception {
        mockMvc.perform(Requests.loginRaw(bodyOf(LIMIT + 1)))
                .andExpect(status().isContentTooLarge())
                .andExpect(header().exists("X-RateLimit-Remaining"));
    }

    private static String bodyOf(int bytes) {
        String body = Requests.loginBody();
        return body + " ".repeat(bytes - body.getBytes(StandardCharsets.UTF_8).length);
    }
}
