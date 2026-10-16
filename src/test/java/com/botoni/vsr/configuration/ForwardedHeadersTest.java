package com.botoni.vsr.configuration;

import com.botoni.vsr.support.Controle;
import org.apache.catalina.Valve;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.RemoteIpValve;
import org.apache.catalina.valves.ValveBase;
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
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * O RemoteIpValve do Tomcat troca o IP da conexão pelo X-Forwarded-For, mas só quando a conexão vem de um
 * proxy listado em TRUSTED_PROXIES. Vazio, nenhum proxy é confiável e o cabeçalho é ignorado.
 */
@DisplayName("Cabeçalhos encaminhados (X-Forwarded-For)")
class ForwardedHeadersTest {

    private static final String PROXY = "10.0.0.5";
    private static final String CLIENT = "203.0.113.7";
    private static final String SPOOFED = "198.51.100.9";

    @Test
    @Controle
    @DisplayName("sem proxy confiável, o IP do cliente é o da conexão e o X-Forwarded-For é ignorado")
    void untrustedConnectionKeepsItsAddress() throws Exception {
        assertThat(client(valve(""), CLIENT, SPOOFED)).isEqualTo(CLIENT);
    }

    @Test
    @Controle
    @DisplayName("a conexão do proxy confiável passa a valer pelo IP do cliente no X-Forwarded-For")
    void trustedProxyForwardsClientAddress() throws Exception {
        assertThat(client(valve("10\\.0\\.0\\.5"), PROXY, CLIENT)).isEqualTo(CLIENT);
    }

    @Test
    @Controle
    @DisplayName("com proxy configurado, quem conecta direto não consegue forjar o IP pelo X-Forwarded-For")
    void directClientCannotSpoofWithTrustedProxyConfigured() throws Exception {
        assertThat(client(valve("10\\.0\\.0\\.5"), CLIENT, SPOOFED)).isEqualTo(CLIENT);
    }

    @Test
    @Controle
    @DisplayName("a rede privada não é confiável por padrão: só o que estiver em TRUSTED_PROXIES")
    void privateNetworkIsNotTrustedByDefault() throws Exception {
        assertThat(client(valve(""), "192.168.0.10", SPOOFED)).isEqualTo("192.168.0.10");
    }

    private static String client(RemoteIpValve valve, String connection, String forwarded) throws Exception {
        AtomicReference<String> seen = new AtomicReference<>();
        valve.setNext(capture(seen));
        valve.invoke(request(connection, forwarded), new Response(new org.apache.coyote.Response()));
        return seen.get();
    }

    private static Valve capture(AtomicReference<String> seen) {
        return new ValveBase() {
            @Override
            public void invoke(Request request, Response response) {
                seen.set(request.getRemoteAddr());
            }
        };
    }

    private static Request request(String connection, String forwarded) {
        Request request = new Request(new Connector(), new org.apache.coyote.Request());
        request.setRemoteAddr(connection);
        request.getCoyoteRequest().getMimeHeaders().addValue("X-Forwarded-For").setString(forwarded);
        return request;
    }

    private static RemoteIpValve valve(String trusted) throws Exception {
        for (Valve valve : engineValves(environment(trusted))) {
            if (valve instanceof RemoteIpValve remoteIp) {
                return remoteIp;
            }
        }
        throw new AssertionError("RemoteIpValve não foi instalado");
    }

    private static Iterable<Valve> engineValves(StandardEnvironment environment) {
        Binder binder = Binder.get(environment);
        ServerProperties server = binder.bind("server", ServerProperties.class).orElseGet(ServerProperties::new);
        TomcatServerProperties tomcat = binder.bind("server.tomcat", TomcatServerProperties.class).orElseGet(TomcatServerProperties::new);
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        new TomcatWebServerFactoryCustomizer(environment, server, tomcat, new WebProperties()).customize(factory);
        return factory.getEngineValves();
    }

    private static StandardEnvironment environment(String trusted) throws Exception {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("env", Map.of("TRUSTED_PROXIES", trusted)));
        new YamlPropertySourceLoader().load("web.yaml", new ClassPathResource("web.yaml"))
                .forEach(environment.getPropertySources()::addLast);
        return environment;
    }
}
