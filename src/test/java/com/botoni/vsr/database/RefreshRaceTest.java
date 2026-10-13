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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Corrida na rotação do refresh token")
class RefreshRaceTest {

    private static final Path MIGRATIONS = Path.of("src/main/resources/db/migration");
    private static final List<String> FILES = List.of("V1__schema.sql", "V2__functions.sql", "V3__ddl.sql",
            "V4__procedures.sql", "V5__triggers.sql", "V6__initial_data.sql");
    private static final String HASH_A = "'\\x" + "aa".repeat(32) + "'::bytea";
    private static final String HASH_B = "'\\x" + "bb".repeat(32) + "'::bytea";
    private static final String HASH_C = "'\\x" + "cc".repeat(32) + "'::bytea";

    private static EmbeddedPostgres postgres;

    @BeforeAll
    static void migrate() throws Exception {
        postgres = EmbeddedPostgres.start();
        try (Connection connection = connection()) {
            for (String file : FILES) {
                execute(connection, Files.readString(MIGRATIONS.resolve(file)).replace("create extension if not exists pg_cron;", ""));
            }
            execute(connection, "insert into pessoas.pessoa (tipo, nome) overriding system value values ('pf', 'Ana')");
            execute(connection, "insert into pessoas.pessoa_fisica (id_pessoa, cpf) values (1, '52998224725')");
            execute(connection, "insert into usuarios.usuario (id_pessoa, email) values (1, 'ana@vsr.com')");
        }
    }

    @AfterAll
    static void stop() throws Exception {
        postgres.close();
    }

    @Test
    @Controle
    @DisplayName("dois usos simultâneos do mesmo token: o banco recusa o perdedor com rn_refresh_token_renovado, que a API trata como reuso")
    void concurrentUseIsDetected() throws Exception {
        int session;
        try (Connection setup = connection()) {
            session = session(setup);
            execute(setup, "call usuarios.emitir_refresh_token(" + session + ", " + HASH_A + ")");
        }

        try (Connection winner = connection(); Connection loser = connection(); Connection observer = connection()) {
            winner.setAutoCommit(false);
            execute(winner, "call usuarios.renovar_refresh_token(" + session + ", " + HASH_A + ", " + HASH_B + ")");

            CompletableFuture<Void> race = CompletableFuture.runAsync(() -> {
                try {
                    execute(loser, "call usuarios.renovar_refresh_token(" + session + ", " + HASH_A + ", " + HASH_C + ")");
                } catch (SQLException exception) {
                    throw new IllegalStateException(exception);
                }
            });
            awaitLock(observer);
            winner.commit();

            assertThatThrownBy(() -> race.get(10, TimeUnit.SECONDS))
                    .rootCause()
                    .isInstanceOf(PSQLException.class)
                    .satisfies(error -> assertThat(((PSQLException) error).getServerErrorMessage().getConstraint())
                            .isEqualTo("rn_refresh_token_renovado"));
        }
    }

    private static void awaitLock(Connection observer) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (System.nanoTime() < deadline) {
            if (!"0".equals(text(observer, "select count(*)::text from pg_stat_activity where wait_event_type = 'Lock'"))) {
                return;
            }
            Thread.onSpinWait();
        }
        throw new IllegalStateException("a segunda renovação não chegou a esperar pelo bloqueio da linha");
    }

    private static int session(Connection connection) throws SQLException {
        execute(connection, "call usuarios.registrar_dispositivo(1, '00000000-0000-0000-0000-0000000000aa', 'ios', 'Apple', 'iPhone', '17')");
        return Integer.parseInt(text(connection, "insert into usuarios.sessao (id_dispositivo, endereco_ip, expira_em) "
                + "select id_dispositivo, '10.0.0.1', now() + interval '30 days' from usuarios.dispositivo "
                + "where identificador = '00000000-0000-0000-0000-0000000000aa' returning id_sessao::text"));
    }

    private static Connection connection() throws SQLException {
        return postgres.getPostgresDatabase().getConnection();
    }

    private static void execute(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private static String text(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery(sql)) {
            result.next();
            return result.getString(1);
        }
    }
}
