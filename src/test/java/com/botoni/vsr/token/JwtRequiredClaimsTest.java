package com.botoni.vsr.token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.botoni.vsr.support.Controle;
import com.botoni.vsr.support.Tokens;
import com.botoni.vsr.support.Users;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Claims obrigatórias do JWT")
class JwtRequiredClaimsTest {

    private static final Algorithm KEY = Algorithm.HMAC256(Base64.getDecoder().decode(Tokens.SECRET));

    @Test
    @Controle
    @DisplayName("token assinado sem exp é recusado")
    void tokenWithoutExpirationIsRejected() {
        String eternal = claims().withIssuedAt(Instant.now()).sign(KEY);

        assertThatThrownBy(() -> Tokens.jwt().verify(eternal)).isInstanceOf(JWTVerificationException.class);
    }

    @Test
    @Controle
    @DisplayName("token assinado sem iat é recusado")
    void tokenWithoutIssuedAtIsRejected() {
        String undated = claims().withExpiresAt(Instant.now().plus(Duration.ofMinutes(15))).sign(KEY);

        assertThatThrownBy(() -> Tokens.jwt().verify(undated)).isInstanceOf(JWTVerificationException.class);
    }

    private static JWTCreator.Builder claims() {
        return JWT.create()
                .withIssuer(Tokens.ISSUER)
                .withSubject(Users.UUID.toString())
                .withClaim("sid", Tokens.SESSION);
    }
}
