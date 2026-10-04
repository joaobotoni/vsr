package com.botoni.vsr.infra.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.botoni.vsr.infra.configuration.properties.JwtProperties;
import java.time.Instant;
import java.util.Base64;
import java.util.function.Function;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final String SESSION_CLAIM = "sid";
    private final JwtProperties properties;
    private final Algorithm algorithm;
    private final JWTVerifier verifier;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.algorithm = algorithm(secret(properties.secretKey()));
        this.verifier = verifier(algorithm, properties.issuer());
    }

    public String issue(UserDetails user, Integer session) {
        if (user == null) {
            throw new IllegalArgumentException("O usuário é obrigatório para emitir o token");
        }
        if (session == null) {
            throw new IllegalArgumentException("A sessão é obrigatória para emitir o token");
        }
        return create(user, session);
    }

    public String subject(String token) {
        return claim(token, DecodedJWT::getSubject);
    }

    public Integer session(String token) {
        return claim(token, jwt -> jwt.getClaim(SESSION_CLAIM).asInt());
    }

    public <T> T claim(String token, Function<DecodedJWT, T> resolver) {
        return resolver.apply(decode(token));
    }

    public long expiration() {
        return properties.expirationTime().toSeconds();
    }

    private String create(UserDetails user, Integer session) {
        Instant now = Instant.now();
        return JWT.create()
                .withIssuer(properties.issuer())
                .withSubject(user.getUsername())
                .withClaim(SESSION_CLAIM, session)
                .withIssuedAt(now)
                .withExpiresAt(now.plus(properties.expirationTime()))
                .sign(algorithm);
    }

    private DecodedJWT decode(String token) {
        if (token == null || token.isBlank()) {
            throw new JWTDecodeException("O token não foi informado");
        }
        return verifier.verify(token);
    }

    private static byte[] secret(String secretKey) {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("A chave JWT não foi configurada");
        }
        return Base64.getDecoder().decode(secretKey);
    }

    private static Algorithm algorithm(byte[] secret) {
        if (secret.length < 32) {
            throw new IllegalStateException("A chave JWT precisa ter pelo menos 256 bits (32 bytes)");
        }
        return Algorithm.HMAC256(secret);
    }

    private static JWTVerifier verifier(Algorithm algorithm, String issuer) {
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalStateException("O issuer do JWT não foi configurado");
        }
        return JWT.require(algorithm)
                .withIssuer(issuer)
                .withClaimPresence(SESSION_CLAIM)
                .acceptLeeway(30)
                .build();
    }
}