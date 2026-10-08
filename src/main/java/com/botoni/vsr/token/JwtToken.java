package com.botoni.vsr.token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.botoni.vsr.exception.custom.JwtException;
import com.botoni.vsr.exception.enums.problem.JwtProblem;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

public final class JwtToken {

    private static final String SESSION_CLAIM = "sid";
    private static final long LEEWAY = 30;
    private static final int MIN_SECRET_BYTES = 32;

    private final String issuer;
    private final Duration expirationTime;
    private final Algorithm algorithm;
    private final JWTVerifier verifier;

    public JwtToken(String secretKey, String issuer, Duration expirationTime) {
        this.issuer = issuer;
        this.expirationTime = expirationTime;
        this.algorithm = algorithm(secret(secretKey));
        this.verifier = verifier(algorithm, issuer);
    }

    public String issue(UUID user, Integer session) {
        if (user == null) {
            throw new JwtException(JwtProblem.MISSING_USER);
        }
        if (session == null) {
            throw new JwtException(JwtProblem.MISSING_SESSION);
        }
        return create(user, session);
    }

    public Claims verify(String token) {
        DecodedJWT jwt = decode(token);
        return new Claims(subject(jwt), session(jwt));
    }

    public long expiration() {
        return expirationTime.toSeconds();
    }

    private String create(UUID user, Integer session) {
        Instant now = Instant.now();
        return JWT.create()
                .withIssuer(issuer)
                .withSubject(user.toString())
                .withClaim(SESSION_CLAIM, session)
                .withIssuedAt(now)
                .withExpiresAt(now.plus(expirationTime))
                .sign(algorithm);
    }

    private DecodedJWT decode(String token) {
        if (token == null || token.isBlank()) {
            throw new JwtException(JwtProblem.MISSING_TOKEN);
        }
        return verifier.verify(token);
    }

    private static UUID subject(DecodedJWT jwt) {
        return uuid(jwt.getSubject());
    }

    private static UUID uuid(String subject) {
        if (subject == null) {
            throw new JwtException(JwtProblem.INVALID_SUBJECT);
        }
        try {
            return UUID.fromString(subject);
        } catch (IllegalArgumentException exception) {
            throw new JwtException(JwtProblem.INVALID_SUBJECT);
        }
    }

    private static Integer session(DecodedJWT jwt) {
        return jwt.getClaim(SESSION_CLAIM).asInt();
    }

    private static byte[] secret(String secretKey) {
        if (secretKey == null || secretKey.isBlank()) {
            throw new JwtException(JwtProblem.MISSING_SECRET);
        }
        try {
            return Base64.getDecoder().decode(secretKey);
        } catch (IllegalArgumentException exception) {
            throw new JwtException(JwtProblem.INVALID_SECRET);
        }
    }

    private static Algorithm algorithm(byte[] secret) {
        if (secret.length < MIN_SECRET_BYTES) {
            throw new JwtException(JwtProblem.WEAK_SECRET);
        }
        return Algorithm.HMAC256(secret);
    }

    private static JWTVerifier verifier(Algorithm algorithm, String issuer) {
        if (issuer == null || issuer.isBlank()) {
            throw new JwtException(JwtProblem.MISSING_ISSUER);
        }
        return JWT.require(algorithm)
                .withIssuer(issuer)
                .withClaimPresence(SESSION_CLAIM)
                .acceptLeeway(LEEWAY)
                .build();
    }
}
