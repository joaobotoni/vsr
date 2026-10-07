package com.botoni.vsr.properties;

import com.botoni.vsr.support.Controle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.UncheckedIOException;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Arquivos de configuração")
class ConfigurationFilesTest {

    private static final String[] RATE_LIMIT_ENVIRONMENT = {
            "RATE_LIMIT_API_CAPACITY=100", "RATE_LIMIT_API_REFILL_RATE=100", "RATE_LIMIT_API_REFILL_INTERVAL=1m",
            "RATE_LIMIT_LOGIN_CAPACITY=5", "RATE_LIMIT_LOGIN_REFILL_RATE=5", "RATE_LIMIT_LOGIN_REFILL_INTERVAL=1m",
            "RATE_LIMIT_REGISTER_CAPACITY=5", "RATE_LIMIT_REGISTER_REFILL_RATE=5", "RATE_LIMIT_REGISTER_REFILL_INTERVAL=1m",
            "RATE_LIMIT_REFRESH_CAPACITY=10", "RATE_LIMIT_REFRESH_REFILL_RATE=10", "RATE_LIMIT_REFRESH_REFILL_INTERVAL=1m",
            "RATE_LIMIT_ACCOUNT_CAPACITY=10", "RATE_LIMIT_ACCOUNT_REFILL_RATE=10", "RATE_LIMIT_ACCOUNT_REFILL_INTERVAL=15m",
            "RATE_LIMIT_PASSWORD_RESET_CAPACITY=5", "RATE_LIMIT_PASSWORD_RESET_REFILL_RATE=5",
            "RATE_LIMIT_PASSWORD_RESET_REFILL_INTERVAL=1m",
            "RATE_LIMIT_UPLOAD_CAPACITY=5", "RATE_LIMIT_UPLOAD_REFILL_RATE=5", "RATE_LIMIT_UPLOAD_REFILL_INTERVAL=1m"
    };

    @Configuration
    @EnableConfigurationProperties(RateLimitProperties.class)
    static class RateLimitOnly {
    }

    @Test
    @Controle
    @DisplayName("rate-limit.yaml define todos os limites usados pela aplicação")
    void rateLimitConfigurationStarts() {
        new ApplicationContextRunner()
                .withUserConfiguration(RateLimitOnly.class)
                .withInitializer(context -> addYaml(context.getEnvironment().getPropertySources(), "rate-limit.yaml"))
                .withPropertyValues(RATE_LIMIT_ENVIRONMENT)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context.getBean(RateLimitProperties.class).refresh()).isNotNull();
                });
    }

    @Test
    @Controle
    @DisplayName("web.yaml ignora cabeçalhos encaminhados")
    void forwardedHeadersAreIgnored() {
        assertThat(property("web.yaml", "server.forward-headers-strategy")).isEqualTo("none");
    }

    @Test
    @Controle
    @DisplayName("database.yaml não imprime SQL no log por padrão")
    void sqlIsNotPrintedByDefault() {
        assertThat(property("database.yaml", "spring.jpa.show-sql")).isEqualTo("false");
    }

    private static String property(String file, String key) {
        StandardEnvironment environment = new StandardEnvironment();
        addYaml(environment.getPropertySources(), file);
        return environment.getProperty(key);
    }

    private static void addYaml(MutablePropertySources sources, String file) {
        try {
            for (PropertySource<?> source : new YamlPropertySourceLoader().load(file, new ClassPathResource(file))) {
                sources.addLast(source);
            }
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
