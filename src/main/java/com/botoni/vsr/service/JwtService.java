package com.botoni.vsr.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.botoni.vsr.configuration.properties.JwtProperties;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.function.Function;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JwtProperties properties;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
    }

    public String issue(UserDetails user) {
        return build(user, properties.expirationTime());
    }

    public String subject(String token) {
        return claim(token, DecodedJWT::getSubject);
    }

    public <T> T claim(String token, Function<DecodedJWT, T> resolver) {
        return resolver.apply(decode(token));
    }

    public long expirationInMillis() {
        return properties.expirationTime().toMillis();
    }

    private String build(UserDetails user, Duration validity) {
        Instant issuedAt = Instant.now();
        return JWT.create()
                .withIssuer(properties.issuer())
                .withSubject(user.getUsername())
                .withIssuedAt(issuedAt)
                .withExpiresAt(issuedAt.plus(validity))
                .sign(algorithm());
    }

    private DecodedJWT decode(String token) {
        return JWT.require(algorithm())
                .withIssuer(properties.issuer())
                .build()
                .verify(token);
    }

    private Algorithm algorithm() {
        byte[] secret = Base64.getDecoder().decode(properties.secretKey());
        return Algorithm.HMAC256(secret);
    }
}