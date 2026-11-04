package com.botoni.vsr.support;

import com.jayway.jsonpath.JsonPath;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.AbstractMockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sobe a aplicação inteira contra um PostgreSQL embutido, compartilhado por todas as subclasses (o contexto do Spring
 * também é reaproveitado). Valida o mapeamento JPA ({@code ddl-auto: validate}), as procedures e os triggers de verdade.
 * O {@code pg_cron} não existe no PostgreSQL embutido: a {@code V7} fica de fora.
 */
@SpringBootTest(properties = "spring.flyway.enabled=false")
@AutoConfigureMockMvc
@TestPropertySource("classpath:security-test.properties")
public abstract class ApplicationTest {

    private static final Path MIGRATIONS = Path.of("src/main/resources/db/migration");
    private static final List<String> FILES = List.of("V1__schema.sql", "V2__functions.sql", "V3__ddl.sql",
            "V4__procedures.sql", "V5__triggers.sql", "V6__initial_data.sql", "V8__imoveis.sql", "V9__vistorias.sql",
            "V10__evidencias.sql");

    private static final String DEVICE = """
            {"identifier":"7f1c2a8e-3b4d-4c5e-8f90-112233445566","platform":"android",
             "manufacturer":"Samsung","model":"S23","osVersion":"14"}""";
    private static final String REGISTER = """
            {"name":"%s","cpf":"%s","email":"%s","password":"Senha@Segura123","device":%s}""";

    private static final EmbeddedPostgres POSTGRES = start();

    /** Diretório temporário das evidências, um por execução. */
    protected static final Path EVIDENCES = directory();

    @Autowired
    protected MockMvc mockMvc;

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("DB_URL", () -> POSTGRES.getJdbcUrl("postgres", "postgres"));
        registry.add("DB_USERNAME", () -> "postgres");
        registry.add("DB_PASSWORD", () -> "postgres");
        registry.add("DB_DRIVER", () -> "org.postgresql.Driver");
        registry.add("DB_POOL_NAME", () -> "test");
        registry.add("DB_POOL_MAX_SIZE", () -> "5");
        registry.add("DB_POOL_MIN_IDLE", () -> "1");
        registry.add("DB_POOL_CONNECTION_TIMEOUT", () -> "30000");
        registry.add("DB_POOL_IDLE_TIMEOUT", () -> "600000");
        registry.add("DB_POOL_MAX_LIFETIME", () -> "1800000");
        registry.add("JPA_DIALECT", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("JPA_BATCH_SIZE", () -> "20");
        registry.add("MAIL_HOST", () -> "localhost");
        registry.add("MAIL_PORT", () -> "2525");
        registry.add("MAIL_USERNAME", () -> "test");
        registry.add("MAIL_PASSWORD", () -> "test");
        registry.add("MAIL_ATTACHMENTS_DIR", () -> System.getProperty("java.io.tmpdir"));
        registry.add("EVIDENCE_DIR", EVIDENCES::toString);
    }

    /** Cadastra o usuário pela API e devolve o access token. */
    protected String register(String name, String cpf, String email) throws Exception {
        String body = mockMvc.perform(json(post("/api/1/auth/register"), REGISTER.formatted(name, cpf, email, DEVICE)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token.accessToken");
    }

    /** Executa a requisição esperando o status e devolve o campo do corpo. */
    protected String read(RequestBuilder request, int expected, String path) throws Exception {
        String body = mockMvc.perform(request)
                .andExpect(status().is(expected))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, path).toString();
    }

    protected static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, String body) {
        return request.contentType(MediaType.APPLICATION_JSON).content(body);
    }

    protected static <B extends AbstractMockHttpServletRequestBuilder<B>> B authorized(B request, String token) {
        return request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }

    protected static void execute(String sql) throws SQLException {
        try (Connection connection = POSTGRES.getPostgresDatabase().getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    protected static String text(String sql) throws SQLException {
        try (Connection connection = POSTGRES.getPostgresDatabase().getConnection();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            result.next();
            return result.getString(1);
        }
    }

    private static EmbeddedPostgres start() {
        try {
            EmbeddedPostgres postgres = EmbeddedPostgres.start();
            migrate(postgres);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> close(postgres)));
            return postgres;
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        } catch (SQLException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static Path directory() {
        try {
            return Files.createTempDirectory("vsr-evidencias");
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private static void migrate(EmbeddedPostgres postgres) throws IOException, SQLException {
        try (Connection connection = postgres.getPostgresDatabase().getConnection();
             Statement statement = connection.createStatement()) {
            for (String file : FILES) {
                statement.execute(Files.readString(MIGRATIONS.resolve(file)).replace("create extension if not exists pg_cron;", ""));
            }
        }
    }

    private static void close(EmbeddedPostgres postgres) {
        try {
            postgres.close();
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
