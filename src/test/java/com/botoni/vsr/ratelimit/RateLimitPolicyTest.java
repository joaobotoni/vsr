package com.botoni.vsr.ratelimit;

import com.botoni.vsr.properties.RateLimitProperties;
import com.botoni.vsr.support.Controle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Escolha da regra de rate limit")
class RateLimitPolicyTest {

    private final RateLimitPolicy policy = RateLimitPolicy.from(new RateLimitProperties(
            limit(100), limit(3), limit(4), limit(5), limit(6), limit(7), limit(8)));

    @AfterEach
    void close() {
        policy.close();
    }

    @Test
    @Controle
    @DisplayName("cada rota protegida usa o próprio limite")
    void routesUseOwnLimit() {
        assertThat(capacityFor("POST", "/api/1/auth/login")).isEqualTo(3);
        assertThat(capacityFor("POST", "/api/1/auth/register")).isEqualTo(4);
        assertThat(capacityFor("POST", "/api/1/auth/refresh")).isEqualTo(5);
        assertThat(capacityFor("PATCH", "/api/1/users/me/password")).isEqualTo(7);
    }

    @Test
    @Controle
    @DisplayName("qualquer outra rota cai no limite geral da API")
    void otherRoutesUseFallback() {
        assertThat(capacityFor("GET", "/api/1/users/me")).isEqualTo(100);
        assertThat(capacityFor("GET", "/api/1/auth/login")).isEqualTo(100);
    }

    private int capacityFor(String method, String path) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setServletPath(path);
        return policy.resolve(request).limit().capacity();
    }

    private static Limit limit(int capacity) {
        return new Limit(capacity, 1, Duration.ofSeconds(1));
    }
}
