package com.botoni.vsr.service;

import com.botoni.vsr.entity.User;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class JwtService {

    public static final String ISSUER = "visura";

    private static final String EXPIRATION_TIME_PROPERTY = "${security.jwt.expiration-time}";
    private static final String EMAIL_CLAIM = "email";
    private static final JwsHeader HEADER = JwsHeader.with(MacAlgorithm.HS256).build();
    private final JwtEncoder jwtEncoder;

    @Getter
    private final long expirationMs;

    public JwtService(JwtEncoder jwtEncoder, @Value(EXPIRATION_TIME_PROPERTY) long expirationMs) {
        this.jwtEncoder = jwtEncoder;
        this.expirationMs = expirationMs;
    }

    public String generateToken(User user) {
        return jwtEncoder.encode(JwtEncoderParameters.from(HEADER, claimsFor(user))).getTokenValue();
    }

    private JwtClaimsSet claimsFor(User user) {
        Instant now = Instant.now();
        return JwtClaimsSet.builder()
                .issuer(ISSUER)
                .issuedAt(now)
                .expiresAt(now.plusMillis(expirationMs))
                .subject(user.getId().toString())
                .claim(EMAIL_CLAIM, user.getEmail())
                .build();
    }
}
