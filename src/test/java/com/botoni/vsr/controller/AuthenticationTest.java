package com.botoni.vsr.controller;

import com.botoni.vsr.EmbeddedPostgresTest;
import com.botoni.vsr.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthenticationTest extends EmbeddedPostgresTest {

    private static final Pattern TOKEN = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"");
    private static final String BASE_URL = "http://localhost:%d%s";
    private static final String SIGNUP_PATH = "/auth/signup";
    private static final String LOGIN_PATH = "/auth/login";
    private static final String ME_PATH = "/users/me";
    private static final String PASSWORD_PATH = "/users/me/password";
    private static final String PATCH = "PATCH";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";

    private static final String NAME = "Ana Souza";
    private static final String FORMATTED_CPF = "529.982.247-25";
    private static final String CPF = "52998224725";
    private static final String RAW_EMAIL = "Ana@Example.com";
    private static final String EMAIL = "ana@example.com";
    private static final String OTHER_EMAIL = "other@example.com";
    private static final String UNKNOWN_EMAIL = "nobody@example.com";
    private static final String MALFORMED_EMAIL = "not-an-email";
    private static final String PASSWORD = "correct-password";
    private static final String WRONG_PASSWORD = "wrong-password";
    private static final String NEW_PASSWORD = "brand-new-password";
    private static final String SHORT_PASSWORD = "short";
    private static final String BCRYPT_PREFIX = "$2";
    private static final String INCORRECT_CURRENT_PASSWORD = "Senha atual incorreta";
    private static final String EXPIRES_IN = "\"expiresInMs\":3600000";
    private static final String PASSWORD_FIELD = "password";

    private static final String SIGNUP_BODY = """
            {"name": "%s", "cpf": "%s", "email": "%s", "password": "%s"}""";
    private static final String LOGIN_BODY = """
            {"email": "%s", "password": "%s"}""";
    private static final String CHANGE_PASSWORD_BODY = """
            {"currentPassword": "%s", "newPassword": "%s"}""";
    private static final String JSON_FIELD = "\"%s\":\"%s\"";
    private static final String CREDENTIAL_QUERY = """
            select c.senha_hash, c.senha_atualizada_em
            from usuarios.credencial_local c
            join usuarios.usuario u on u.id_usuario = c.id_usuario
            where u.email = ?""";
    private static final String HASH_COLUMN = "senha_hash";
    private static final String UPDATED_AT_COLUMN = "senha_atualizada_em";

    private static final int OK = 200;
    private static final int CREATED = 201;
    private static final int NO_CONTENT = 204;
    private static final int BAD_REQUEST = 400;
    private static final int UNAUTHORIZED = 401;
    private static final int CONFLICT = 409;
    private static final int UNPROCESSABLE_CONTENT = 422;

    private final HttpClient client = HttpClient.newHttpClient();
    private final JdbcTemplate jdbcTemplate;
    private final AuthenticationManager authenticationManager;
    private final int port;

    @Autowired
    AuthenticationTest(JdbcTemplate jdbcTemplate, AuthenticationManager authenticationManager, @LocalServerPort int port) {
        this.jdbcTemplate = jdbcTemplate;
        this.authenticationManager = authenticationManager;
        this.port = port;
    }

    @Test
    void signupLoginMeAndPasswordChange() throws Exception {
        Map<String, Object> credential = assertSignup();
        assertPasswordErasedAfterAuthentication();
        String token = assertLogin();
        assertMe(token);
        assertPasswordChange(token, credential);
    }

    private Map<String, Object> assertSignup() throws Exception {
        HttpResponse<String> signup = post(SIGNUP_PATH, SIGNUP_BODY.formatted(NAME, FORMATTED_CPF, RAW_EMAIL, PASSWORD));
        assertStatus(CREATED, signup);
        assertTrue(signup.body().contains(jsonField("email", EMAIL)), signup.body());
        assertFalse(signup.body().contains(PASSWORD_FIELD), signup.body());
        assertStatus(CONFLICT, post(SIGNUP_PATH, SIGNUP_BODY.formatted(NAME, CPF, OTHER_EMAIL, PASSWORD)));

        Map<String, Object> credential = credentialOf(EMAIL);
        assertTrue(credential.get(HASH_COLUMN).toString().startsWith(BCRYPT_PREFIX));
        assertNotNull(credential.get(UPDATED_AT_COLUMN));
        return credential;
    }

    private void assertPasswordErasedAfterAuthentication() {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(EMAIL, PASSWORD));
        User principal = (User) authentication.getPrincipal();
        assertEquals(EMAIL, principal.getEmail());
        assertNull(principal.getPassword());
        assertNull(authentication.getCredentials());
    }

    private String assertLogin() throws Exception {
        assertStatus(UNAUTHORIZED, login(EMAIL, WRONG_PASSWORD));
        assertStatus(UNAUTHORIZED, login(UNKNOWN_EMAIL, PASSWORD));
        assertStatus(UNAUTHORIZED, login(MALFORMED_EMAIL, PASSWORD));

        HttpResponse<String> login = login(RAW_EMAIL, PASSWORD);
        assertStatus(OK, login);
        assertTrue(login.body().contains(EXPIRES_IN), login.body());
        return tokenOf(login);
    }

    private void assertMe(String token) throws Exception {
        HttpResponse<String> me = get(ME_PATH, token);
        assertStatus(OK, me);
        assertTrue(me.body().contains(jsonField("name", NAME)), me.body());
        assertTrue(me.body().contains(jsonField("cpf", CPF)), me.body());
        assertFalse(me.body().contains(PASSWORD_FIELD), me.body());
        assertStatus(UNAUTHORIZED, client.send(request(ME_PATH).GET().build(), HttpResponse.BodyHandlers.ofString()));
    }

    private void assertPasswordChange(String token, Map<String, Object> previousCredential) throws Exception {
        HttpResponse<String> wrongCurrent = changePassword(token, WRONG_PASSWORD, NEW_PASSWORD);
        assertStatus(UNPROCESSABLE_CONTENT, wrongCurrent);
        assertTrue(wrongCurrent.body().contains(INCORRECT_CURRENT_PASSWORD), wrongCurrent.body());
        assertStatus(BAD_REQUEST, changePassword(token, PASSWORD, SHORT_PASSWORD));
        assertStatus(NO_CONTENT, changePassword(token, PASSWORD, NEW_PASSWORD));

        Map<String, Object> updated = credentialOf(EMAIL);
        assertTrue(updated.get(HASH_COLUMN).toString().startsWith(BCRYPT_PREFIX));
        assertNotEquals(previousCredential.get(HASH_COLUMN), updated.get(HASH_COLUMN));
        assertNotEquals(previousCredential.get(UPDATED_AT_COLUMN), updated.get(UPDATED_AT_COLUMN));

        assertStatus(UNAUTHORIZED, login(EMAIL, PASSWORD));
        assertStatus(OK, login(EMAIL, NEW_PASSWORD));
        assertStatus(UNAUTHORIZED, changePassword(null, NEW_PASSWORD, PASSWORD));
    }

    private HttpResponse<String> login(String email, String password) throws IOException, InterruptedException {
        return post(LOGIN_PATH, LOGIN_BODY.formatted(email, password));
    }

    private HttpResponse<String> changePassword(String token, String currentPassword, String newPassword)
            throws IOException, InterruptedException {
        HttpRequest.Builder builder = jsonRequest(PASSWORD_PATH)
                .method(PATCH, HttpRequest.BodyPublishers.ofString(CHANGE_PASSWORD_BODY.formatted(currentPassword, newPassword)));
        return client.send(withToken(builder, token).build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String path, String json) throws IOException, InterruptedException {
        HttpRequest httpRequest = jsonRequest(path).POST(HttpRequest.BodyPublishers.ofString(json)).build();
        return client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> get(String path, String token) throws IOException, InterruptedException {
        return client.send(withToken(request(path), token).GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpRequest.Builder jsonRequest(String path) {
        return request(path).header(CONTENT_TYPE, APPLICATION_JSON);
    }

    private HttpRequest.Builder request(String path) {
        return HttpRequest.newBuilder(URI.create(BASE_URL.formatted(port, path)));
    }

    private static HttpRequest.Builder withToken(HttpRequest.Builder builder, String token) {
        if (token == null) {
            return builder;
        }
        return builder.header(AUTHORIZATION, BEARER + token);
    }

    private Map<String, Object> credentialOf(String email) {
        return jdbcTemplate.queryForMap(CREDENTIAL_QUERY, email);
    }

    private static String tokenOf(HttpResponse<String> login) {
        Matcher matcher = TOKEN.matcher(login.body());
        assertTrue(matcher.find(), login.body());
        return matcher.group(1);
    }

    private static String jsonField(String name, String value) {
        return JSON_FIELD.formatted(name, value);
    }

    private static void assertStatus(int expected, HttpResponse<String> response) {
        assertEquals(expected, response.statusCode(), response.body());
    }
}
