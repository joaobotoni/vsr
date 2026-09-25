package com.botoni.vsr.configuration;

import com.botoni.vsr.service.JwtService;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
public class JwtConfiguration {

    private static final String SECRET_KEY_PROPERTY = "${security.jwt.secret-key}";
    private static final String KEY_TOO_SHORT = "security.jwt.secret-key must be at least 256 bits (32 bytes, Base64-encoded)";
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int MIN_KEY_LENGTH_BYTES = 32;

    @Bean
    SecretKey jwtSecretKey(@Value(SECRET_KEY_PROPERTY) String base64Key) {
        byte[] key = Base64.getDecoder().decode(base64Key);
        if (isKeyTooShort(key)) {
            throw new IllegalStateException(KEY_TOO_SHORT);
        }
        return new SecretKeySpec(key, HMAC_ALGORITHM);
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey));
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(JwtService.ISSUER));
        return decoder;
    }

    private static boolean isKeyTooShort(byte[] key) {
        return key.length < MIN_KEY_LENGTH_BYTES;
    }
}
