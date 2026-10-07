package com.botoni.vsr.support;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.botoni.vsr.security.JwtToken;

import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

public final class Tokens {

    public static final String SECRET = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";
    public static final String REFRESH_SECRET = "ZmVkY2JhOTg3NjU0MzIxMGZlZGNiYTk4NzY1NDMyMTA=";
    public static final String ISSUER = "vsr-test";
    public static final int SESSION = 10;

    private static final Algorithm KEY = Algorithm.HMAC256(Base64.getDecoder().decode(SECRET));
    private static final Duration LIFETIME = Duration.ofMinutes(15);

    private Tokens() {
    }

    public static JwtToken jwt() {
        return new JwtToken(SECRET, ISSUER, LIFETIME);
    }

    public static String valid() {
        return jwt().issue(Users.principal(), SESSION);
    }

    public static String forged() {
        return claims().sign(Algorithm.HMAC256("uma-outra-chave-com-pelo-menos-32-bytes"));
    }

    public static String unsigned() {
        return claims().sign(Algorithm.none());
    }

    public static String expired() {
        return claims().withExpiresAt(Instant.now().minus(Duration.ofHours(1))).sign(KEY);
    }

    public static String wrongIssuer() {
        return claims().withIssuer("outro-emissor").sign(KEY);
    }

    public static String withoutSession() {
        return JWT.create().withIssuer(ISSUER).withSubject(Users.EMAIL).withExpiresAt(Instant.now().plus(LIFETIME)).sign(KEY);
    }

    private static JWTCreator.Builder claims() {
        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(Users.EMAIL)
                .withClaim("sid", SESSION)
                .withExpiresAt(Instant.now().plus(LIFETIME));
    }
}
