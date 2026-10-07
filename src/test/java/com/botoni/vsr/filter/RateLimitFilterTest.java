package com.botoni.vsr.filter;

import com.botoni.vsr.support.Controle;
import com.botoni.vsr.support.Ips;
import com.botoni.vsr.support.WebSecurityTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static com.botoni.vsr.support.Requests.from;
import static com.botoni.vsr.support.Requests.login;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebSecurityTest
@DisplayName("Rate limit do login")
class RateLimitFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Value("${RATE_LIMIT_LOGIN_CAPACITY}")
    private int capacity;

    @Test
    @Controle
    @DisplayName("ao estourar o limite responde 429 com Retry-After")
    void exceedingLimitIsRejected() throws Exception {
        String ip = Ips.next();
        exhaust(ip);

        loginFrom(ip)
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists(HttpHeaders.RETRY_AFTER))
                .andExpect(jsonPath("$.code").value("RateLimitProblem.EXCEEDED"));
    }

    @Test
    @Controle
    @DisplayName("toda resposta informa o limite e o saldo restante")
    void responsesCarryLimitHeaders() throws Exception {
        loginFrom(Ips.next())
                .andExpect(header().string("X-RateLimit-Limit", String.valueOf(capacity)))
                .andExpect(header().string("X-RateLimit-Remaining", String.valueOf(capacity - 1)));
    }

    @Test
    @Controle
    @DisplayName("trocar de IP libera o limite do filtro, que é contado por IP")
    void filterLimitIsPerIp() throws Exception {
        String ip = Ips.next();
        exhaust(ip);
        loginFrom(ip).andExpect(status().isTooManyRequests());

        loginFrom(Ips.next()).andExpect(status().isOk());
    }

    @Test
    @Controle
    @DisplayName("rotacionar endereços IPv6 do mesmo /64 atinge o limite")
    void ipv6RotationHitsLimit() throws Exception {
        for (int i = 0; i < capacity; i++) {
            loginFrom(Ips.nextV6InSameNetwork()).andExpect(status().isOk());
        }
        loginFrom(Ips.nextV6InSameNetwork()).andExpect(status().isTooManyRequests());
    }

    private void exhaust(String ip) throws Exception {
        for (int i = 0; i < capacity; i++) {
            loginFrom(ip).andExpect(status().isOk());
        }
    }

    private ResultActions loginFrom(String ip) throws Exception {
        return mockMvc.perform(login().with(from(ip)));
    }
}
