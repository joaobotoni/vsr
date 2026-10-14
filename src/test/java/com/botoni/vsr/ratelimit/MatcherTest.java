package com.botoni.vsr.ratelimit;

import com.botoni.vsr.exception.custom.MatcherException;
import com.botoni.vsr.exception.enums.problem.MatcherProblem;
import com.botoni.vsr.support.Controle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Casamento de requisições do rate limit")
class MatcherTest {

    @Test
    @Controle
    @DisplayName("a rota casa com o método e o caminho em qualquer versão da API")
    void routeMatchesAnyVersion() {
        Matcher login = Matcher.from(new Route(HttpMethod.POST, "/auth/login"));

        assertThat(login.matches(request("POST", "/api/1/auth/login"))).isTrue();
        assertThat(login.matches(request("POST", "/api/2/auth/login"))).isTrue();
    }

    @Test
    @Controle
    @DisplayName("outro método ou outro caminho não casam")
    void otherMethodOrPathDoesNotMatch() {
        Matcher login = Matcher.from(new Route(HttpMethod.POST, "/auth/login"));

        assertThat(login.matches(request("GET", "/api/1/auth/login"))).isFalse();
        assertThat(login.matches(request("POST", "/api/1/auth/register"))).isFalse();
        assertThat(login.matches(request("POST", "/auth/login"))).isFalse();
    }

    @Test
    @Controle
    @DisplayName("any casa com qualquer requisição")
    void anyMatchesEverything() {
        assertThat(Matcher.any().matches(request("DELETE", "/qualquer/coisa"))).isTrue();
    }

    @Test
    @Controle
    @DisplayName("matcher sem RequestMatcher impede a subida")
    void missingDelegateIsRejected() {
        assertThatThrownBy(() -> new Matcher(null))
                .isInstanceOf(MatcherException.class)
                .extracting("problem").isEqualTo(MatcherProblem.MISSING_DELEGATE);
    }

    private static MockHttpServletRequest request(String method, String path) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setServletPath(path);
        return request;
    }
}
