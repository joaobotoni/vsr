package com.botoni.vsr.support;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public final class Requests {

    public static final String VALID_PASSWORD = "senha segura 123";

    private static final String DEVICE = """
            {"identifier":"7f1c2a8e-3b4d-4c5e-8f90-112233445566","platform":"android",
             "manufacturer":"Samsung","model":"S23","osVersion":"14"}""";

    private static final String LOGIN = """
            {"email":"%s","password":"%s","device":%s}""";

    private static final String REGISTER = """
            {"name":"Ana","cpf":"529.982.247-25","email":"%s","password":"%s","device":%s}""";

    private Requests() {
    }

    public static MockHttpServletRequestBuilder me() {
        return get("/api/1/users/me").with(from(Ips.next()));
    }

    public static MockHttpServletRequestBuilder me(String token) {
        return me().header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }

    public static MockHttpServletRequestBuilder logout(String token) {
        return post("/api/1/auth/logout").with(from(Ips.next())).header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }

    public static MockHttpServletRequestBuilder login() {
        return json(post("/api/1/auth/login"), LOGIN.formatted(Users.EMAIL, VALID_PASSWORD, DEVICE));
    }

    public static MockHttpServletRequestBuilder register() {
        return register(VALID_PASSWORD);
    }

    public static MockHttpServletRequestBuilder register(String password) {
        return json(post("/api/1/auth/register"), REGISTER.formatted(Users.EMAIL, password, DEVICE));
    }

    public static MockHttpServletRequestBuilder registerRaw(String body) {
        return json(post("/api/1/auth/register"), body);
    }

    public static RequestPostProcessor from(String ip) {
        return request -> {
            request.setRemoteAddr(ip);
            return request;
        };
    }

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, String body) {
        return request.with(from(Ips.next())).contentType(MediaType.APPLICATION_JSON).content(body);
    }
}
