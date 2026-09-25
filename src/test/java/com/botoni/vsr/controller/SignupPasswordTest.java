package com.botoni.vsr.controller;

import com.botoni.vsr.EmbeddedPostgresTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SignupPasswordTest extends EmbeddedPostgresTest {

    private static final String SIGNUP_PATH = "/auth/signup";
    private static final String SIGNUP_BODY = """
            {"name": "Gabriel Costa", "cpf": "%s", "email": "%s", "password": "%s"}""";
    private static final String SIGNUP_BODY_WITHOUT_PASSWORD = """
            {"name": "Gabriel Costa", "cpf": "12345678909", "email": "sem-senha@example.com"}""";
    private static final String VALID_CPF = "12345678909";
    private static final String VALID_EMAIL = "gabriel@example.com";
    private static final String OTHER_CPF = "98765432100";
    private static final String OTHER_EMAIL = "curta@example.com";
    private static final String VALID_PASSWORD = "correct-password";
    private static final String SHORT_PASSWORD = "short";
    private static final String PASSWORD_FIELD = "password";

    private final MockMvc mockMvc;

    @Autowired
    SignupPasswordTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void shortPasswordReturns400() throws Exception {
        signup(SIGNUP_BODY.formatted(OTHER_CPF, OTHER_EMAIL, SHORT_PASSWORD))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(not(containsString(SHORT_PASSWORD))));
    }

    @Test
    void missingPasswordReturns400() throws Exception {
        signup(SIGNUP_BODY_WITHOUT_PASSWORD).andExpect(status().isBadRequest());
    }

    @Test
    void validPasswordReturns201() throws Exception {
        signup(SIGNUP_BODY.formatted(VALID_CPF, VALID_EMAIL, VALID_PASSWORD))
                .andExpect(status().isCreated())
                .andExpect(content().string(not(containsString(PASSWORD_FIELD))));
    }

    private ResultActions signup(String body) throws Exception {
        return mockMvc.perform(post(SIGNUP_PATH).contentType(MediaType.APPLICATION_JSON).content(body));
    }
}
