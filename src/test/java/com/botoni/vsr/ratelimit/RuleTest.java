package com.botoni.vsr.ratelimit;

import com.botoni.vsr.properties.RateLimitProperties;
import com.botoni.vsr.support.Controle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Rate limit por regra")
class RuleTest {

    private static final String KEY = "ip:10.0.0.1";

    @Test
    @Controle
    @DisplayName("consome até a capacidade, recusa e volta a liberar após o refund")
    void consumeAndRefund() {
        try (Rule rule = Rule.any(new Limit(2, 1, Duration.ofHours(1)))) {
            assertThat(rule.consume(KEY).allowed()).isTrue();
            assertThat(rule.consume(KEY).allowed()).isTrue();
            assertThat(rule.consume(KEY).exceeded()).isTrue();

            rule.refund(KEY);

            assertThat(rule.consume(KEY).allowed()).isTrue();
        }
    }

    @Test
    @Controle
    @DisplayName("a recusa informa um tempo de nova tentativa dentro do intervalo")
    void rejectionRetryWithinInterval() {
        try (Rule rule = Rule.any(new Limit(1, 1, Duration.ofMinutes(1)))) {
            rule.consume(KEY);

            assertThat(rule.consume(KEY).retry()).isBetween(1L, 60L);
        }
    }

    @Test
    @Controle
    @DisplayName("fechar descarta os saldos")
    void closeDiscardsBuckets() {
        Rule rule = Rule.any(new Limit(1, 1, Duration.ofHours(1)));
        rule.consume(KEY);

        rule.close();

        assertThat(rule.consume(KEY).allowed()).isTrue();
    }

    @Test
    @Controle
    @DisplayName("cada regra da política tem saldo próprio: estourar o login não afeta o cadastro nem o geral")
    void policyRulesAreIndependent() {
        Limit one = new Limit(1, 1, Duration.ofHours(1));
        try (RateLimitPolicy policy = RateLimitPolicy.from(new RateLimitProperties(one, one, one, one, one, one, one))) {
            Rule login = policy.resolve(request("POST", "/api/1/auth/login"));
            login.consume(KEY);

            assertThat(login.consume(KEY).exceeded()).isTrue();
            assertThat(policy.resolve(request("POST", "/api/1/auth/register")).consume(KEY).allowed()).isTrue();
            assertThat(policy.resolve(request("GET", "/api/1/users/me")).consume(KEY).allowed()).isTrue();
        }
    }

    @Test
    @Controle
    @DisplayName("a mesma rota resolve sempre a mesma regra, inclusive em versões diferentes da API")
    void sameRouteSharesRule() {
        Limit one = new Limit(1, 1, Duration.ofHours(1));
        try (RateLimitPolicy policy = RateLimitPolicy.from(new RateLimitProperties(one, one, one, one, one, one, one))) {
            policy.resolve(request("POST", "/api/1/auth/login")).consume(KEY);

            assertThat(policy.resolve(request("POST", "/api/2/auth/login")).consume(KEY).exceeded()).isTrue();
        }
    }

    private static MockHttpServletRequest request(String method, String path) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setServletPath(path);
        return request;
    }
}
