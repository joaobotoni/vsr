package com.botoni.vsr.filter;

import com.botoni.vsr.service.LoginService;
import com.botoni.vsr.support.Controle;
import com.botoni.vsr.support.Ips;
import com.botoni.vsr.support.Requests;
import com.botoni.vsr.support.WebSecurityTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockingDetails;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebSecurityTest
@DisplayName("Variações de rota contra o rate limit do login")
class RateLimitRouteBypassTest {

    private static final int ATTEMPTS = 10;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LoginService loginService;

    @Value("${RATE_LIMIT_LOGIN_CAPACITY}")
    private int capacity;

    @Controle
    @ParameterizedTest(name = "{0}")
    @DisplayName("nenhuma grafia alternativa da rota chega ao login mais vezes que o limite")
    @ValueSource(strings = {
            "/api/1/auth/login",
            "/api/1/auth/login/",
            "/api/qualquer/auth/login",
            "/api/1/auth/LOGIN",
            "/api/1/auth/login.json",
            "/api/1/auth/%6cogin",
            "/api/1/auth//login",
            "/api/1/auth/./login",
            "/api/1/auth/login;x=1",
            "/api/1/auth/x/../login"
    })
    void alternativeSpellingsDoNotBypassLimit(String path) throws Exception {
        Mockito.clearInvocations(loginService);
        String ip = Ips.next();
        for (int i = 0; i < ATTEMPTS; i++) {
            try {
                mockMvc.perform(post(path).with(Requests.from(ip))
                        .contentType(MediaType.APPLICATION_JSON).content(Requests.loginBody()));
            } catch (Exception rejected) {
                // o firewall do Spring Security recusa a URL antes dos filtros
            }
        }

        long reached = mockingDetails(loginService).getInvocations().stream()
                .filter(invocation -> invocation.getMethod().getName().equals("login")).count();
        assertThat(reached).as("chamadas ao login por %s", path).isLessThanOrEqualTo(capacity);
    }
}
