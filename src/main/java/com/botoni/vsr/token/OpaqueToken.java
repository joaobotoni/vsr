package com.botoni.vsr.token;

import com.botoni.vsr.exception.custom.OpaqueTokenException;
import com.botoni.vsr.exception.enums.problem.OpaqueTokenProblem;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

public final class OpaqueToken {

    private static final int TOKEN_BYTES = 32;
    private static final int MIN_SECRET_BYTES = 32;
    private static final String HASH_ALGORITHM = "HmacSHA256";

    private final SecureRandom random = new SecureRandom();
    private final SecretKeySpec key;

    public OpaqueToken(String secretKey) {
        this.key = key(secret(secretKey));
        mac(key);
    }

    public String generate() {
        return encode(random());
    }

    public byte[] hash(String token) {
        return mac(key).doFinal(token.getBytes(StandardCharsets.UTF_8));
    }

    private byte[] random() {
        byte[] bytes = new byte[TOKEN_BYTES];
        random.nextBytes(bytes);
        return bytes;
    }

    private static String encode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static byte[] secret(String secretKey) {
        if (secretKey == null || secretKey.isBlank()) {
            throw new OpaqueTokenException(OpaqueTokenProblem.MISSING_SECRET);
        }
        try {
            return Base64.getDecoder().decode(secretKey);
        } catch (IllegalArgumentException exception) {
            throw new OpaqueTokenException(OpaqueTokenProblem.INVALID_SECRET);
        }
    }

    private static SecretKeySpec key(byte[] secret) {
        if (secret.length < MIN_SECRET_BYTES) {
            throw new OpaqueTokenException(OpaqueTokenProblem.WEAK_SECRET);
        }
        return new SecretKeySpec(secret, HASH_ALGORITHM);
    }

    private static Mac mac(SecretKeySpec key) {
        try {
            Mac mac = Mac.getInstance(HASH_ALGORITHM);
            mac.init(key);
            return mac;
        } catch (GeneralSecurityException exception) {
            throw new OpaqueTokenException(OpaqueTokenProblem.MISSING_ALGORITHM);
        }
    }
}
