package com.botoni.vsr.ratelimit;

import com.botoni.vsr.exception.custom.RouteException;
import com.botoni.vsr.exception.enums.problem.RouteProblem;
import com.botoni.vsr.support.Controle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Rotas do rate limit")
class RouteTest {

    @Test
    @Controle
    @DisplayName("o padrão da rota vale para qualquer versão da API")
    void patternIsVersioned() {
        assertThat(new Route(HttpMethod.POST, "/auth/login").pattern()).isEqualTo("/api/*/auth/login");
    }

    @Test
    @Controle
    @DisplayName("rota sem método impede a subida")
    void missingMethodIsRejected() {
        assertThatThrownBy(() -> new Route(null, "/auth/login"))
                .isInstanceOf(RouteException.class)
                .extracting("problem").isEqualTo(RouteProblem.MISSING_METHOD);
    }

    @Test
    @Controle
    @DisplayName("caminho sem barra inicial ou ausente impede a subida")
    void invalidPathIsRejected() {
        assertThatThrownBy(() -> new Route(HttpMethod.POST, "auth/login"))
                .isInstanceOf(RouteException.class)
                .extracting("problem").isEqualTo(RouteProblem.INVALID_PATH);
        assertThatThrownBy(() -> new Route(HttpMethod.POST, null))
                .isInstanceOf(RouteException.class)
                .extracting("problem").isEqualTo(RouteProblem.INVALID_PATH);
    }

    @Test
    @Controle
    @DisplayName("sem método e com caminho inválido, o método é validado primeiro")
    void methodIsValidatedFirst() {
        assertThatThrownBy(() -> new Route(null, "auth/login"))
                .isInstanceOf(RouteException.class)
                .extracting("problem").isEqualTo(RouteProblem.MISSING_METHOD);
    }
}
