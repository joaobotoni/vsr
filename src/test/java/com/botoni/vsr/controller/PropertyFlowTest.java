package com.botoni.vsr.controller;

import com.botoni.vsr.support.Controle;
import com.jayway.jsonpath.JsonPath;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sobe a aplicação inteira contra um PostgreSQL embutido: valida o mapeamento JPA ({@code ddl-auto: validate}),
 * a função de endereço, a remoção lógica e o isolamento entre donos.
 */
@SpringBootTest(properties = "spring.flyway.enabled=false")
@AutoConfigureMockMvc
@TestPropertySource("classpath:security-test.properties")
@DisplayName("Imóveis pela API, com banco de verdade")
class PropertyFlowTest {

    private static final Path MIGRATIONS = Path.of("src/main/resources/db/migration");
    private static final List<String> FILES = List.of("V1__schema.sql", "V2__functions.sql", "V3__ddl.sql",
            "V4__procedures.sql", "V5__triggers.sql", "V6__initial_data.sql", "V8__imoveis.sql");

    private static final String DEVICE = """
            {"identifier":"7f1c2a8e-3b4d-4c5e-8f90-112233445566","platform":"android",
             "manufacturer":"Samsung","model":"S23","osVersion":"14"}""";
    private static final String REGISTER = """
            {"name":"%s","cpf":"%s","email":"%s","password":"Senha@Segura123","device":%s}""";
    private static final String PROPERTY = """
            {"description":"  Apartamento   de frente ","type":"apartamento","category":"residencial",
             "address":{"cep":"01310-100","state":"sp","city":"%s","district":"Bela Vista",
                        "street":"Avenida Paulista","number":"1000","complement":"Apto 12"}}""";

    private static EmbeddedPostgres postgres;

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) throws Exception {
        postgres = EmbeddedPostgres.start();
        try (Connection connection = postgres.getPostgresDatabase().getConnection()) {
            for (String file : FILES) {
                execute(connection, Files.readString(MIGRATIONS.resolve(file)).replace("create extension if not exists pg_cron;", ""));
            }
        }
        registry.add("DB_URL", () -> postgres.getJdbcUrl("postgres", "postgres"));
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
    }

    @AfterAll
    static void stop() throws Exception {
        postgres.close();
    }

    @Test
    @Controle
    @DisplayName("o dono cadastra, consulta, lista, edita e remove; outro usuário recebe 404 em tudo")
    void ownerManagesPropertyAndOthersSeeNothing() throws Exception {
        String ana = register("Ana Souza", "52998224725", "ana.imovel@vsr.com");
        String bia = register("Bia Lima", "11144477735", "bia.imovel@vsr.com");

        String body = mockMvc.perform(authorized(json(post("/api/1/properties"), PROPERTY.formatted("São Paulo")), ana))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Apartamento de frente"))
                .andExpect(jsonPath("$.address.cep").value("01310100"))
                .andExpect(jsonPath("$.address.state").value("SP"))
                .andExpect(jsonPath("$.address.city").value("São Paulo"))
                .andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(body, "$.id");
        String property = "/api/1/properties/" + id;

        mockMvc.perform(authorized(json(post("/api/1/properties"), PROPERTY.formatted("são paulo")), ana))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("UniqueConstraint.UQ_IMOVEL_ENDERECO_TITULAR"));

        mockMvc.perform(authorized(get("/api/1/properties"), ana))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0].id").value(id));

        mockMvc.perform(authorized(get("/api/1/properties"), bia))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0));
        mockMvc.perform(authorized(get(property), bia))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PropertyProblem.NOT_FOUND"));
        mockMvc.perform(authorized(json(patch(property), "{\"description\":\"invasão\"}"), bia))
                .andExpect(status().isNotFound());
        mockMvc.perform(authorized(delete(property), bia))
                .andExpect(status().isNotFound());

        mockMvc.perform(authorized(json(patch(property), "{\"description\":\"Cobertura\",\"category\":\"alto_padrao\"}"), ana))
                .andExpect(status().isOk());
        mockMvc.perform(authorized(get(property), ana))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Cobertura"))
                .andExpect(jsonPath("$.category").value("alto_padrao"))
                .andExpect(jsonPath("$.type").value("apartamento"));

        mockMvc.perform(authorized(delete(property), ana))
                .andExpect(status().isNoContent());
        mockMvc.perform(authorized(get(property), ana))
                .andExpect(status().isNotFound());
        mockMvc.perform(authorized(get("/api/1/properties"), ana))
                .andExpect(jsonPath("$.total").value(0));

        mockMvc.perform(authorized(json(post("/api/1/properties"), PROPERTY.formatted("São Paulo")), ana))
                .andExpect(status().isCreated());
    }

    @Test
    @Controle
    @DisplayName("imóvel com vistoria em andamento não é removido")
    void propertyWithOpenInspectionIsKept() throws Exception {
        String carla = register("Carla Dias", "39053344705", "carla.imovel@vsr.com");
        String body = mockMvc.perform(authorized(json(post("/api/1/properties"), PROPERTY.formatted("Campinas")), carla))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(body, "$.id");
        try (Connection connection = postgres.getPostgresDatabase().getConnection()) {
            execute(connection, "insert into vistorias.vistoria (id_usuario_responsavel, id_imovel, tipo) "
                    + "select id_usuario, id_imovel, 'entrada' from imoveis.imovel where uuid = '" + id + "'");
        }

        mockMvc.perform(authorized(delete("/api/1/properties/" + id), carla))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RuleConstraint.RN_IMOVEL_VISTORIA_ANDAMENTO"));
        mockMvc.perform(authorized(get("/api/1/properties/" + id), carla))
                .andExpect(status().isOk());
    }

    @Test
    @Controle
    @DisplayName("parâmetros inválidos respondem 400")
    void invalidParametersAreRejected() throws Exception {
        String dani = register("Dani Rocha", "86288366757", "dani.imovel@vsr.com");

        mockMvc.perform(authorized(get("/api/1/properties/nao-e-uuid"), dani))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ValidationProblem.INVALID_PARAMETER"));
        mockMvc.perform(authorized(get("/api/1/properties").param("size", "500"), dani))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ValidationProblem.INVALID_DATA"));
        mockMvc.perform(authorized(json(post("/api/1/properties"), PROPERTY.formatted("São Paulo").replace("01310-100", "123")), dani))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CepProblem.FORMAT"));
        mockMvc.perform(authorized(json(post("/api/1/properties"), PROPERTY.formatted("São Paulo").replace("\"sp\"", "\"XX\"")), dani))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ValidationProblem.INVALID_DATA"));
        mockMvc.perform(authorized(json(post("/api/1/properties"), PROPERTY.formatted("<script>")), dani))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Controle
    @DisplayName("sem token, a rota de imóveis responde 401")
    void anonymousIsRejected() throws Exception {
        mockMvc.perform(get("/api/1/properties"))
                .andExpect(status().isUnauthorized());
    }

    private String register(String name, String cpf, String email) throws Exception {
        String body = mockMvc.perform(json(post("/api/1/auth/register"), REGISTER.formatted(name, cpf, email, DEVICE)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token.accessToken");
    }

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, String body) {
        return request.contentType(MediaType.APPLICATION_JSON).content(body);
    }

    private static MockHttpServletRequestBuilder authorized(MockHttpServletRequestBuilder request, String token) {
        return request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }

    private static void execute(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
}
