package com.botoni.vsr.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final String SECRET_KEY_PROPERTY = "${security.jwt.secret-key}";

    private static final String ISSUER_PROPERTY = "${security.jwt.issuer}";

    private static final String EXPIRATION_TIME_PROPERTY = "${security.jwt.expiration-time}";

    @Value(SECRET_KEY_PROPERTY)
    private String secretKey;

    @Value(ISSUER_PROPERTY)
    private String issuer;

    @Value(EXPIRATION_TIME_PROPERTY)
    private long jwtExpiration;

    public String extractUsername(String token) {
        return extractClaim(token, DecodedJWT::getSubject);
    }

    public <T> T extractClaim(String token, Function<DecodedJWT, T> claimsResolver) {
        final DecodedJWT decodedJWT = extractAllClaims(token);
        return claimsResolver.apply(decodedJWT);
    }

    public String generateToken(UserDetails userDetails) {
        return buildToken(userDetails, jwtExpiration);
    }

    public long getExpirationTime() {
        return jwtExpiration;
    }

    private String buildToken(UserDetails userDetails, long expiration) {
        return JWT.create()
                .withIssuer(issuer)
                .withSubject(userDetails.getUsername())
                .withIssuedAt(new Date(System.currentTimeMillis()))
                .withExpiresAt(new Date(System.currentTimeMillis() + expiration))
                .sign(getSignInAlgorithm());
    }

    private DecodedJWT extractAllClaims(String token) {
        return JWT.require(getSignInAlgorithm())
                .withIssuer(issuer)
                .build()
                .verify(token);
    }

    private Algorithm getSignInAlgorithm() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        return Algorithm.HMAC256(keyBytes);
    }
}