package com.botoni.vsr.security;

import com.botoni.vsr.exception.custom.OpaqueTokenException;
import com.botoni.vsr.exception.enums.problem.OpaqueTokenProblem;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public final class OpaqueToken {

    private static final int TOKEN_BYTES = 32;
    private static final String HASH_ALGORITHM = "SHA-256";

    private final SecureRandom random = new SecureRandom();

    public String generate() {
        return encode(randomBytes());
    }

    public byte[] hash(String token) {
        return digest().digest(token.getBytes(StandardCharsets.UTF_8));
    }

    private byte[] randomBytes() {
        byte[] bytes = new byte[TOKEN_BYTES];
        random.nextBytes(bytes);
        return bytes;
    }

    private static String encode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static MessageDigest digest() {
        try {
            return MessageDigest.getInstance(HASH_ALGORITHM);
        } catch (NoSuchAlgorithmException exception) {
            throw new OpaqueTokenException(OpaqueTokenProblem.MISSING_ALGORITHM);
        }
    }
}
