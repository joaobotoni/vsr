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
            "V4__procedures.sql", "V5__triggers.sql", "V6__initial_data.sql", "V8__imoveis.sql");
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
    void passwordHashMustBeArgon2id() {
        for (String hash : List.of("$2a$10$bcrypt", "$argon2i$v=19$hash", "$argon2d$v=19$hash")) {
            assertThatThrownBy(() -> execute("update usuarios.credencial_local set senha_hash = '" + hash + "' where id_usuario = 1"))
                    .isInstanceOf(PSQLException.class)
                    .satisfies(exception -> assertThat(((PSQLException) exception).getServerErrorMessage().getConstraint())
                            .isEqualTo("ck_credencial_local_hash"));
        }
    }

    @Test
    void sessionLimitRevokesOldest() throws Exception {
        execute("insert into pessoas.pessoa (tipo, nome) overriding system value values ('pf', 'Bia')");
        execute("insert into pessoas.pessoa_fisica (id_pessoa, cpf) values (2, '11144477735')");
        int user = Integer.parseInt(text("insert into usuarios.usuario (id_pessoa, email) values (2, 'bia@vsr.com') returning id_usuario::text"));
        int oldest = open(user, "00000000-0000-0000-0000-0000000000b1");
        int middle = open(user, "00000000-0000-0000-0000-0000000000b2");
        int newest = open(user, "00000000-0000-0000-0000-0000000000b3");
        int other = session("00000000-0000-0000-0000-0000000000b4");

        execute("call usuarios.limitar_sessoes_usuario(" + user + ", 2)");

        assertThat(text("select (revogada_em is null)::text from usuarios.sessao where id_sessao = " + oldest)).isEqualTo("false");
        assertThat(text("select (revogada_em is null)::text from usuarios.sessao where id_sessao = " + middle)).isEqualTo("true");
        assertThat(text("select (revogada_em is null)::text from usuarios.sessao where id_sessao = " + newest)).isEqualTo("true");
        assertThat(text("select (revogada_em is null)::text from usuarios.sessao where id_sessao = " + other)).isEqualTo("true");
    }

    @Test
    void addressIsReusedIgnoringCase() throws Exception {
        String first = text("select enderecos.registrar_endereco('SP', 'São Paulo', 'Bela Vista', '01310100', 'Avenida Paulista', '1000', null)::text");
        String same = text("select enderecos.registrar_endereco('SP', 'SÃO PAULO', 'bela vista', '01310100', 'avenida paulista', '1000', null)::text");
        String other = text("select enderecos.registrar_endereco('SP', 'São Paulo', 'Bela Vista', '01310100', 'Avenida Paulista', '1000', 'Apto 1')::text");
        assertThat(same).isEqualTo(first);
        assertThat(other).isNotEqualTo(first);
        assertThat(text("select count(*)::text from enderecos.cidade where lower(nome) = 'são paulo'")).isEqualTo("1");
    }

    @Test
    void addressRequiresExistingState() {
        assertRule("select enderecos.registrar_endereco('XX', 'Cidade', 'Bairro', '01310100', 'Rua', '1', null)", "rn_estado_existente");
    }

    @Test
    void propertyRemovalIsLogicalAndFreesTheAddress() throws Exception {
        String address = text("select enderecos.registrar_endereco('RJ', 'Rio de Janeiro', 'Centro', '20040002', 'Rua da Assembleia', '10', null)::text");
        String insert = "insert into imoveis.imovel (id_endereco, id_usuario, tipo, categoria) values (" + address
                + ", 1, 'casa', 'residencial') returning id_imovel::text";
        int property = Integer.parseInt(text(insert));
        execute("insert into vistorias.vistoria (id_usuario_responsavel, id_imovel, tipo) values (1, " + property + ", 'entrada')");
        assertRule("call imoveis.remover_imovel(" + property + ")", "rn_imovel_vistoria_andamento");

        execute("update vistorias.vistoria set status = 'finalizada', finalizada_em = now() where id_imovel = " + property);
        execute("call imoveis.remover_imovel(" + property + ")");
        assertThat(text("select (removido_em is not null)::text from imoveis.imovel where id_imovel = " + property)).isEqualTo("true");
        assertThat(text(insert)).isNotEqualTo(String.valueOf(property));
    }

    @Test
    void cleanupRuns() throws Exception {
        execute("call rotinas.limpar_sessoes_expiradas()");
    }

    private static int open(int user, String identifier) throws SQLException {
        execute("call usuarios.registrar_dispositivo(" + user + ", '" + identifier + "', 'android', 'Samsung', 'S23', '14')");
        return Integer.parseInt(text("insert into usuarios.sessao (id_dispositivo, endereco_ip, expira_em) "
                + "select id_dispositivo, '10.0.0.2', now() + interval '30 days' from usuarios.dispositivo "
                + "where identificador = '" + identifier + "' returning id_sessao::text"));
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
