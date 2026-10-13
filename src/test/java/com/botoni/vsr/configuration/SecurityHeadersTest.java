package com.botoni.vsr.configuration;

import com.botoni.vsr.support.Controle;
import com.botoni.vsr.support.Requests;
import com.botoni.vsr.support.WebSecurityTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@WebSecurityTest
@DisplayName("Cabeçalhos de segurança da resposta")
class SecurityHeadersTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Controle
    @DisplayName("resposta que carrega tokens não pode ser guardada em cache nem farejada")
    void tokenResponseIsNotCached() throws Exception {
        mockMvc.perform(Requests.login())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, containsString("no-store")))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"));
    }

    @Test
    @Controle
    @DisplayName("HSTS vai em toda resposta, mesmo atrás de um proxy que termina o TLS")
    void hstsIsAlwaysSent() throws Exception {
        mockMvc.perform(Requests.login())
                .andExpect(header().string("Strict-Transport-Security", "max-age=31536000 ; includeSubDomains"));
    }
}
