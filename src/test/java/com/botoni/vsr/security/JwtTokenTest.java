package com.botoni.vsr.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.botoni.vsr.exception.custom.JwtException;
import com.botoni.vsr.exception.enums.problem.JwtProblem;
import com.botoni.vsr.support.Controle;
import com.botoni.vsr.support.Tokens;
import com.botoni.vsr.support.Users;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Emissão e verificação de JWT")
class JwtTokenTest {

    @Test
    @Controle
    @DisplayName("token emitido é verificado com usuário e sessão")
    void issuedTokenRoundTrips() {
        JwtToken.Claims claims = Tokens.jwt().verify(Tokens.valid());

        assertThat(claims.subject()).isEqualTo(Users.EMAIL);
        assertThat(claims.session()).isEqualTo(Tokens.SESSION);
    }

    @Test
    @Controle
    @DisplayName("payload adulterado é rejeitado")
    void tamperedPayloadIsRejected() {
        String[] parts = Tokens.valid().split("\\.");
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8)
                .replace(Users.EMAIL, "admin@vsr.com");
        String tampered = parts[0] + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8)) + "." + parts[2];

        assertThatThrownBy(() -> Tokens.jwt().verify(tampered)).isInstanceOf(JWTVerificationException.class);
    }

    @Controle
    @ParameterizedTest(name = "configuração com {0} impede a subida ({2})")
    @CsvSource(delimiter = '|', value = {
            "chave curta (16 bytes)     | MDEyMzQ1Njc4OWFiY2RlZg== | WEAK_SECRET",
            "chave fora de Base64       | não-é-base64!            | INVALID_SECRET",
            "chave vazia                | ' '                      | MISSING_SECRET"
    })
    void insecureSecretIsRejected(String description, String secret, JwtProblem problem) {
        assertThatThrownBy(() -> new JwtToken(secret, Tokens.ISSUER, Duration.ofMinutes(15)))
                .isInstanceOf(JwtException.class)
                .extracting("problem").isEqualTo(problem);
    }

    @Test
    @Controle
    @DisplayName("emissor vazio impede a subida")
    void blankIssuerIsRejected() {
        assertThatThrownBy(() -> new JwtToken(Tokens.SECRET, " ", Duration.ofMinutes(15)))
                .isInstanceOf(JwtException.class)
                .extracting("problem").isEqualTo(JwtProblem.MISSING_ISSUER);
    }
}
