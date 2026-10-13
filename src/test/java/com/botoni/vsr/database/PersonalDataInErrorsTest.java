package com.botoni.vsr.database;

import com.botoni.vsr.support.Controle;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PSQLException;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * O Hibernate registra em log, nível ERROR, a mensagem do driver de toda violação de constraint
 * (SqlExceptionHelper). A mensagem não pode trazer o valor duplicado nem a linha recusada.
 */
@DisplayName("Dados pessoais nas mensagens de erro do banco")
class PersonalDataInErrorsTest {

    private static final Path MIGRATIONS = Path.of("src/main/resources/db/migration");
    private static final List<String> FILES = List.of("V1__schema.sql", "V2__functions.sql", "V3__ddl.sql");
    private static final String DETAIL = "logServerErrorDetail";
    private static final String CPF = "52998224725";
    private static final String EMAIL = "ana@vsr.com";
    private static final String INVALID_EMAIL = "Bia.Invalida@VSR";

    private static EmbeddedPostgres postgres;
    private static Connection connection;

    @BeforeAll
    static void migrate() throws Exception {
        postgres = EmbeddedPostgres.start();
        connection = DriverManager.getConnection(postgres.getJdbcUrl("postgres", "postgres"), driverProperties());
        for (String file : FILES) {
            execute(Files.readString(MIGRATIONS.resolve(file)).replace("create extension if not exists pg_cron;", ""));
        }
        execute("insert into pessoas.pessoa (tipo, nome) overriding system value values ('pf', 'Ana'), ('pf', 'Bia')");
        execute("insert into pessoas.pessoa_fisica (id_pessoa, cpf) values (1, '" + CPF + "')");
        execute("insert into usuarios.usuario (id_pessoa, email) values (1, '" + EMAIL + "')");
    }

    @AfterAll
    static void stop() throws Exception {
        connection.close();
        postgres.close();
    }

    @Test
    @Controle
    @DisplayName("database.yaml desliga o detalhe das mensagens de erro do driver")
    void detailIsDisabled() {
        assertThat(driverProperties().getProperty(DETAIL)).isEqualTo("false");
    }

    @Test
    @Controle
    @DisplayName("CPF duplicado: a mensagem cita a constraint, mas não o CPF")
    void duplicateCpfHidesValue() {
        assertThatThrownBy(() -> execute("insert into pessoas.pessoa_fisica (id_pessoa, cpf) values (2, '" + CPF + "')"))
                .isInstanceOf(PSQLException.class)
                .hasMessageContaining("uq_pessoa_fisica_cpf")
                .hasMessageNotContaining(CPF);
    }

    @Test
    @Controle
    @DisplayName("e-mail duplicado: a mensagem cita a constraint, mas não o e-mail")
    void duplicateEmailHidesValue() {
        assertThatThrownBy(() -> execute("insert into usuarios.usuario (id_pessoa, email) values (2, '" + EMAIL + "')"))
                .isInstanceOf(PSQLException.class)
                .hasMessageContaining("uq_usuario_email")
                .hasMessageNotContaining(EMAIL);
    }

    @Test
    @Controle
    @DisplayName("violação de check não despeja a linha recusada (\"Failing row contains\")")
    void checkViolationHidesRow() {
        assertThatThrownBy(() -> execute("insert into usuarios.usuario (id_pessoa, email) values (2, '" + INVALID_EMAIL + "')"))
                .isInstanceOf(PSQLException.class)
                .hasMessageContaining("ck_usuario_email")
                .hasMessageNotContaining(INVALID_EMAIL);
    }

    private static Properties driverProperties() {
        StandardEnvironment environment = new StandardEnvironment();
        try {
            for (PropertySource<?> source : new YamlPropertySourceLoader().load("database.yaml", new ClassPathResource("database.yaml"))) {
                environment.getPropertySources().addLast(source);
            }
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
        Properties properties = new Properties();
        properties.setProperty(DETAIL, environment.getProperty("spring.datasource.hikari.data-source-properties." + DETAIL));
        return properties;
    }

    private static void execute(String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
}
