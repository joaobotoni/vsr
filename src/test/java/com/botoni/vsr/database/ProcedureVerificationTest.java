package com.botoni.vsr.database;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PSQLException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProcedureVerificationTest {

    private static final Path MIGRATIONS = Path.of("src/main/resources/db/migration");
    private static final List<String> FILES = List.of("V1__schema.sql", "V2__functions.sql", "V3__ddl.sql",
            "V4__procedures.sql", "V5__triggers.sql", "V6__initial_data.sql");
    private static final String HASH_A = "'\\x" + "aa".repeat(32) + "'::bytea";
    private static final String HASH_B = "'\\x" + "bb".repeat(32) + "'::bytea";
    private static final String HASH_C = "'\\x" + "cc".repeat(32) + "'::bytea";

    private static EmbeddedPostgres postgres;
    private static Connection connection;

    @BeforeAll
    static void migrate() throws Exception {
        postgres = EmbeddedPostgres.start();
        connection = postgres.getPostgresDatabase().getConnection();
        for (String file : FILES) {
            String sql = Files.readString(MIGRATIONS.resolve(file)).replace("create extension if not exists pg_cron;", "");
            execute(sql);
        }
        execute("insert into pessoas.pessoa (tipo, nome) overriding system value values ('pf', 'Ana')");
        execute("insert into pessoas.pessoa_fisica (id_pessoa, cpf) values (1, '52998224725')");
        execute("insert into usuarios.usuario (id_pessoa, email) values (1, 'ana@vsr.com')");
        execute("insert into usuarios.credencial_local (id_usuario, senha_hash) values (1, '$argon2id$atual')");
    }

    @AfterAll
    static void stop() throws Exception {
        connection.close();
        postgres.close();
    }

    @Test
    void deviceUpsertDoesNotRewriteUnchangedRow() throws Exception {
        String device = "call usuarios.registrar_dispositivo(1, '00000000-0000-0000-0000-000000000001', 'android', 'Samsung', 'S23', '14')";
        execute(device);
        String before = text("select xmin::text from usuarios.dispositivo where identificador = '00000000-0000-0000-0000-000000000001'");
        execute(device);
        assertThat(text("select xmin::text from usuarios.dispositivo where identificador = '00000000-0000-0000-0000-000000000001'")).isEqualTo(before);
        execute("call usuarios.registrar_dispositivo(1, '00000000-0000-0000-0000-000000000001', 'android', 'Samsung', 'S24', '15')");
        assertThat(text("select modelo from usuarios.dispositivo where identificador = '00000000-0000-0000-0000-000000000001'")).isEqualTo("S24");
    }

    @Test
    void refreshTokenRules() throws Exception {
        int session = session("00000000-0000-0000-0000-000000000002");
        execute("call usuarios.emitir_refresh_token(" + session + ", " + HASH_A + ")");
        execute("call usuarios.renovar_refresh_token(" + session + ", " + HASH_A + ", " + HASH_B + ")");
        assertRule("call usuarios.renovar_refresh_token(" + session + ", " + HASH_A + ", " + HASH_C + ")", "rn_refresh_token_renovado");
        execute("call usuarios.revogar_sessao(" + session + ")");
        assertRule("call usuarios.renovar_refresh_token(" + session + ", " + HASH_B + ", " + HASH_C + ")", "rn_sessao_ativa");
        assertRule("call usuarios.registrar_acesso_sessao(" + session + ")", "rn_sessao_ativa");
        int revoked = session("00000000-0000-0000-0000-000000000003");
        execute("call usuarios.revogar_sessao(" + revoked + ")");
        assertRule("call usuarios.emitir_refresh_token(" + revoked + ", " + HASH_C + ")", "rn_sessao_ativa");
        assertRule("call usuarios.emitir_refresh_token(999999, " + HASH_C + ")", "rn_sessao_ativa");
    }

    @Test
    void revokeAllWhenKeptSessionIsNull() throws Exception {
        int first = session("00000000-0000-0000-0000-000000000004");
        int second = session("00000000-0000-0000-0000-000000000005");
        execute("call usuarios.revogar_sessoes_usuario(1, " + first + ")");
        assertThat(text("select (revogada_em is null)::text from usuarios.sessao where id_sessao = " + first)).isEqualTo("true");
        assertThat(text("select (revogada_em is null)::text from usuarios.sessao where id_sessao = " + second)).isEqualTo("false");
        execute("call usuarios.revogar_sessoes_usuario(1, null)");
        assertThat(text("select (revogada_em is null)::text from usuarios.sessao where id_sessao = " + first)).isEqualTo("false");
    }

    @Test
    void passwordChangeRule() throws Exception {
        assertRule("call usuarios.trocar_senha(1, 'hash-errado', '$argon2id$novo')", "rn_senha_alterada");
        execute("call usuarios.trocar_senha(1, '$argon2id$atual', '$argon2id$novo')");
        assertThat(text("select senha_hash from usuarios.credencial_local where id_usuario = 1")).isEqualTo("$argon2id$novo");
    }

    @Test
    void cleanupRuns() throws Exception {
        execute("call rotinas.limpar_sessoes_expiradas()");
    }

    private static int session(String identifier) throws SQLException {
        execute("call usuarios.registrar_dispositivo(1, '" + identifier + "', 'ios', 'Apple', 'iPhone', '17')");
        return Integer.parseInt(text("insert into usuarios.sessao (id_dispositivo, endereco_ip, expira_em) "
                + "select id_dispositivo, '10.0.0.1', now() + interval '30 days' from usuarios.dispositivo "
                + "where identificador = '" + identifier + "' returning id_sessao::text"));
    }

    private static void assertRule(String sql, String rule) {
        assertThatThrownBy(() -> execute(sql))
                .isInstanceOf(PSQLException.class)
                .satisfies(exception -> {
                    PSQLException error = (PSQLException) exception;
                    assertThat(error.getSQLState()).isEqualTo("23514");
                    assertThat(error.getServerErrorMessage().getConstraint()).isEqualTo(rule);
                    assertThat(error.getServerErrorMessage().getMessage()).isEqualTo("violates check constraint \"" + rule + "\"");
                });
    }

    private static void execute(String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private static String text(String sql) throws SQLException {
        try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery(sql)) {
            result.next();
            return result.getString(1);
        }
    }
}
