package com.botoni.vsr.database;

import com.botoni.vsr.support.Controle;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PSQLException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Role mínima da aplicação no banco")
class AppRoleTest {

    private static final Path MIGRATIONS = Path.of("src/main/resources/db/migration");
    private static final Path ROLE = Path.of("docs/db/app-role.sql");
    private static final List<String> FILES = List.of("V1__schema.sql", "V2__functions.sql", "V3__ddl.sql",
            "V4__procedures.sql", "V5__triggers.sql", "V6__initial_data.sql");
    private static final String INSUFFICIENT_PRIVILEGE = "42501";

    private static EmbeddedPostgres postgres;
    private static Connection owner;
    private static Connection app;

    @BeforeAll
    static void migrate() throws Exception {
        postgres = EmbeddedPostgres.start();
        owner = postgres.getPostgresDatabase().getConnection();
        for (String file : FILES) {
            execute(owner, Files.readString(MIGRATIONS.resolve(file)).replace("create extension if not exists pg_cron;", ""));
        }
        execute(owner, Files.readString(ROLE).replace(":'app_password'", "'senha-de-teste'"));
        execute(owner, "insert into pessoas.pessoa (tipo, nome) overriding system value values ('pf', 'Ana')");
        execute(owner, "insert into pessoas.pessoa_fisica (id_pessoa, cpf) values (1, '52998224725')");
        execute(owner, "insert into usuarios.usuario (id_pessoa, email) values (1, 'ana@vsr.com')");
        app = postgres.getDatabase("vsr_app", "postgres").getConnection();
    }

    @AfterAll
    static void stop() throws Exception {
        app.close();
        owner.close();
        postgres.close();
    }

    @Test
    @Controle
    @DisplayName("a aplicação lê, grava e chama as procedures que usa")
    void appRunsItsOperations() {
        assertThatCode(() -> {
            execute(app, "select count(*) from usuarios.usuario");
            execute(app, "select count(*) from catalogo.tipo_item");
            execute(app, "call usuarios.registrar_dispositivo(1, '00000000-0000-0000-0000-0000000000b1', 'android', 'Samsung', 'S23', '14')");
            execute(app, "insert into usuarios.sessao (id_dispositivo, endereco_ip, expira_em) select id_dispositivo, '10.0.0.1', now() + interval '30 days' from usuarios.dispositivo");
            execute(app, "call usuarios.revogar_sessoes_usuario(1, null)");
        }).doesNotThrowAnyException();
    }

    @Test
    @Controle
    @DisplayName("a aplicação não apaga linhas")
    void appCannotDelete() {
        assertDenied("delete from usuarios.sessao");
        assertDenied("delete from usuarios.usuario");
    }

    @Test
    @Controle
    @DisplayName("a aplicação não altera o catálogo, que é só leitura")
    void catalogIsReadOnly() {
        assertDenied("insert into catalogo.tipo_item (nome, padrao) values ('Teste', false)");
    }

    @Test
    @Controle
    @DisplayName("a aplicação não cria nem altera objetos do banco")
    void appCannotChangeSchema() {
        assertDenied("create table usuarios.invasora (id int)");
        assertDenied("alter table usuarios.usuario add column extra text");
        assertDenied("drop table usuarios.sessao");
    }

    @Test
    @Controle
    @DisplayName("a aplicação não roda a limpeza de sessões, que é do job do pg_cron")
    void appCannotRunCleanup() {
        assertDenied("call rotinas.limpar_sessoes_expiradas()");
    }

    private static void assertDenied(String sql) {
        assertThatThrownBy(() -> execute(app, sql))
                .isInstanceOf(PSQLException.class)
                .satisfies(exception -> org.assertj.core.api.Assertions
                        .assertThat(((PSQLException) exception).getSQLState()).isEqualTo(INSUFFICIENT_PRIVILEGE));
    }

    private static void execute(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
}
