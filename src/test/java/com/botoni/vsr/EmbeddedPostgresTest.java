package com.botoni.vsr;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.io.UncheckedIOException;

public abstract class EmbeddedPostgresTest {

    private static final String DATABASE_USER = "postgres";
    private static final String DATABASE_NAME = "postgres";
    private static final String JWT_SECRET_KEY = "dGVzdC1zZWNyZXQta2V5LXdpdGgtYXQtbGVhc3QtMzItYnl0ZXM=";
    private static final String JWT_EXPIRATION_TIME = "3600000";
    private static final EmbeddedPostgres POSTGRES = start();

    private static EmbeddedPostgres start() {
        try {
            return EmbeddedPostgres.start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> POSTGRES.getJdbcUrl(DATABASE_USER, DATABASE_NAME));
        registry.add("spring.datasource.username", () -> DATABASE_USER);
        registry.add("spring.datasource.password", () -> DATABASE_USER);
        registry.add("security.jwt.secret-key", () -> JWT_SECRET_KEY);
        registry.add("security.jwt.expiration-time", () -> JWT_EXPIRATION_TIME);
    }
}
