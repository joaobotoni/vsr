package com.botoni.vsr.configuration;

import com.botoni.vsr.support.Controle;
import org.apache.catalina.valves.RemoteIpValve;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.boot.tomcat.autoconfigure.TomcatServerProperties;
import org.springframework.boot.tomcat.autoconfigure.TomcatWebServerFactoryCustomizer;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * O RemoteIpValve do Tomcat é quem troca o IP da conexão pelo X-Forwarded-For. O Spring Boot só o instala
 * quando server.forward-headers-strategy é native.
 */
@DisplayName("Cabeçalhos encaminhados (X-Forwarded-For)")
class ForwardedHeadersTest {

    @Test
    @Controle
    @DisplayName("o IP do cliente é sempre o da conexão e o X-Forwarded-For é ignorado")
    void currentConfigurationIgnoresForwardedHeaders() throws Exception {
        assertThat(engineValves(environment())).noneMatch(RemoteIpValve.class::isInstance);
    }

    @Test
    @Controle
    @DisplayName("com forward-headers-strategy native o Tomcat passaria a confiar no X-Forwarded-For")
    void nativeStrategyWouldTrustForwardedHeaders() throws Exception {
        StandardEnvironment environment = environment();
        environment.getPropertySources().addFirst(new MapPropertySource("native",
                Map.of("server.forward-headers-strategy", "native")));

        assertThat(engineValves(environment)).anyMatch(RemoteIpValve.class::isInstance);
    }

    private static Iterable<?> engineValves(StandardEnvironment environment) {
        Binder binder = Binder.get(environment);
        ServerProperties server = binder.bind("server", ServerProperties.class).orElseGet(ServerProperties::new);
        TomcatServerProperties tomcat = binder.bind("server.tomcat", TomcatServerProperties.class).orElseGet(TomcatServerProperties::new);
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        new TomcatWebServerFactoryCustomizer(environment, server, tomcat, new WebProperties()).customize(factory);
        return factory.getEngineValves();
    }

    private static StandardEnvironment environment() throws Exception {
        StandardEnvironment environment = new StandardEnvironment();
        new YamlPropertySourceLoader().load("web.yaml", new ClassPathResource("web.yaml"))
                .forEach(environment.getPropertySources()::addLast);
        return environment;
    }
}
