package com.botoni.vsr.ratelimit;

import com.botoni.vsr.ratelimit.TokenBucket.Limit;
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

    private static final String KEY = "ip:10.0.0.1";

    private final RateLimitPolicy policy = RateLimitPolicy.from(new RateLimitProperties(
            limit(100), limit(3), limit(4), limit(5), limit(6), limit(9), limit(7)));

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
        assertThat(capacityFor("POST", "/auth/login")).isEqualTo(100);
    }

    @Test
    @Controle
    @DisplayName("cada rota tem saldo próprio: estourar o login não afeta o cadastro nem o geral")
    void routesHaveOwnBalance() {
        Limit one = new Limit(1, 1, Duration.ofHours(1));
        try (RateLimitPolicy strict = RateLimitPolicy.from(new RateLimitProperties(one, one, one, one, one, new Limit(2, 1, Duration.ofHours(1)), one))) {
            TokenBucket login = strict.resolve(request("POST", "/api/1/auth/login"));
            login.allow(KEY);

            assertThat(login.allow(KEY).allowed()).isFalse();
            assertThat(strict.resolve(request("POST", "/api/1/auth/register")).allow(KEY).allowed()).isTrue();
            assertThat(strict.resolve(request("GET", "/api/1/users/me")).allow(KEY).allowed()).isTrue();
        }
    }

    @Test
    @Controle
    @DisplayName("a mesma rota resolve sempre o mesmo saldo, inclusive em versões diferentes da API")
    void sameRouteSharesBalance() {
        Limit one = new Limit(1, 1, Duration.ofHours(1));
        try (RateLimitPolicy strict = RateLimitPolicy.from(new RateLimitProperties(one, one, one, one, one, new Limit(2, 1, Duration.ofHours(1)), one))) {
            strict.resolve(request("POST", "/api/1/auth/login")).allow(KEY);

            assertThat(strict.resolve(request("POST", "/api/2/auth/login")).allow(KEY).allowed()).isFalse();
        }
    }

    private int capacityFor(String method, String path) {
        return policy.resolve(request(method, path)).limit().capacity();
    }

    private static MockHttpServletRequest request(String method, String path) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setServletPath(path);
        return request;
    }

    private static Limit limit(int capacity) {
        return new Limit(capacity, 1, Duration.ofSeconds(1));
    }
}
