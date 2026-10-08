package com.botoni.vsr.ratelimit;

import com.botoni.vsr.exception.custom.RouteException;
import com.botoni.vsr.exception.enums.problem.RouteProblem;
import com.botoni.vsr.support.Controle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Rotas do rate limit")
class RouteTest {

    @Test
    @Controle
    @DisplayName("a rota casa com o método e o caminho em qualquer versão da API")
    void routeMatchesAnyVersion() {
        Route login = Route.of(HttpMethod.POST, "/auth/login");

        assertThat(login.matches(request("POST", "/api/1/auth/login"))).isTrue();
        assertThat(login.matches(request("POST", "/api/2/auth/login"))).isTrue();
    }

    @Test
    @Controle
    @DisplayName("outro método ou outro caminho não casam")
    void otherMethodOrPathDoesNotMatch() {
        Route login = Route.of(HttpMethod.POST, "/auth/login");

        assertThat(login.matches(request("GET", "/api/1/auth/login"))).isFalse();
        assertThat(login.matches(request("POST", "/api/1/auth/register"))).isFalse();
        assertThat(login.matches(request("POST", "/auth/login"))).isFalse();
    }

    @Test
    @Controle
    @DisplayName("any casa com qualquer requisição")
    void anyMatchesEverything() {
        assertThat(Route.any().matches(request("DELETE", "/qualquer/coisa"))).isTrue();
    }

    @Test
    @Controle
    @DisplayName("rota sem método impede a subida")
    void missingMethodIsRejected() {
        assertThatThrownBy(() -> Route.of(null, "/auth/login"))
                .isInstanceOf(RouteException.class)
                .extracting("problem").isEqualTo(RouteProblem.MISSING_METHOD);
    }

    @Test
    @Controle
    @DisplayName("caminho sem barra inicial ou ausente impede a subida")
    void invalidPathIsRejected() {
        assertThatThrownBy(() -> Route.of(HttpMethod.POST, "auth/login"))
                .isInstanceOf(RouteException.class)
                .extracting("problem").isEqualTo(RouteProblem.INVALID_PATH);
        assertThatThrownBy(() -> Route.of(HttpMethod.POST, null))
                .isInstanceOf(RouteException.class)
                .extracting("problem").isEqualTo(RouteProblem.INVALID_PATH);
    }

    @Test
    @Controle
    @DisplayName("rota sem matcher impede a subida")
    void missingMatcherIsRejected() {
        assertThatThrownBy(() -> new Route(null))
                .isInstanceOf(RouteException.class)
                .extracting("problem").isEqualTo(RouteProblem.MISSING_MATCHER);
    }

    private static MockHttpServletRequest request(String method, String path) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setServletPath(path);
        return request;
    }
}
