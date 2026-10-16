package com.botoni.vsr.filter;

import com.botoni.vsr.exception.custom.RequestException;
import com.botoni.vsr.exception.enums.problem.RequestProblem;
import com.botoni.vsr.properties.RequestProperties;
import com.botoni.vsr.service.LoginService;
import com.botoni.vsr.support.Controle;
import com.botoni.vsr.support.Requests;
import com.botoni.vsr.support.Tokens;
import com.botoni.vsr.support.WebSecurityTest;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.unit.DataSize;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @Test
    @Controle
    @DisplayName("corpo sem Content-Length acima do limite é recusado com 413 antes de seguir na cadeia")
    void undeclaredBodyAboveLimitIsRejected() {
        MockFilterChain chain = new MockFilterChain();

        assertThatThrownBy(() -> filter().doFilter(undeclared(LIMIT + 1), new MockHttpServletResponse(), chain))
                .isInstanceOfSatisfying(RequestException.class,
                        exception -> assertThat(exception.problem()).isEqualTo(RequestProblem.BODY_TOO_LARGE));
        assertThat(chain.getRequest()).isNull();
    }

    @Test
    @Controle
    @DisplayName("corpo sem Content-Length dentro do limite segue inteiro, pelo stream e pelo reader")
    void undeclaredBodyWithinLimitIsReplayed() throws Exception {
        MockFilterChain chain = new MockFilterChain();

        filter().doFilter(undeclared(LIMIT), new MockHttpServletResponse(), chain);

        HttpServletRequest forwarded = (HttpServletRequest) chain.getRequest();
        assertThat(forwarded.getContentLengthLong()).isEqualTo(LIMIT);
        assertThat(FileCopyUtils.copyToString(forwarded.getReader())).isEqualTo(bodyOf(LIMIT));
    }

    private static RequestBodySizeLimitFilter filter() {
        return new RequestBodySizeLimitFilter(new RequestProperties(DataSize.ofBytes(LIMIT)));
    }

    private static MockHttpServletRequest undeclared(int bytes) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/1/auth/login") {
            @Override
            public int getContentLength() {
                return -1;
            }

            @Override
            public long getContentLengthLong() {
                return -1;
            }
        };
        request.setContent(bodyOf(bytes).getBytes(StandardCharsets.UTF_8));
        return request;
    }

    private static String bodyOf(int bytes) {
        String body = Requests.loginBody();
        return body + " ".repeat(bytes - body.getBytes(StandardCharsets.UTF_8).length);
    }
}
