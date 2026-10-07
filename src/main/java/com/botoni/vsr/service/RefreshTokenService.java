package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.RefreshToken;
import com.botoni.vsr.database.entity.Session;
import com.botoni.vsr.database.repository.RefreshTokenRepository;
import com.botoni.vsr.exception.custom.RefreshTokenException;
import com.botoni.vsr.exception.enums.problem.RefreshTokenProblem;
import com.botoni.vsr.security.OpaqueToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final OpaqueToken opaqueToken;

    @Transactional
    public String issue(Session session) {
        String token = generate();
        persist(session, token);
        return token;
    }

    @Transactional(readOnly = true)
    public RefreshToken find(String token) {
        byte[] digest = hash(token);
        return findByHash(digest);
    }

    @Transactional
    public String renew(RefreshToken stored) {
        if (isExpired(stored)) {
            throw new RefreshTokenException(RefreshTokenProblem.EXPIRED);
        }
        String token = generate();
        rotate(stored, token);
        return token;
    }

    public boolean isReused(RefreshToken stored, String token) {
        return !Arrays.equals(stored.getCurrentHash(), hash(token));
    }

    private String generate() {
        return opaqueToken.generate();
    }

    private void persist(Session session, String token) {
        refreshTokenRepository.issue(session.getId(), hash(token));
    }

    private byte[] hash(String token) {
        return opaqueToken.hash(token);
    }

    private RefreshToken findByHash(byte[] hash) {
        return refreshTokenRepository.findByCurrentHash(hash)
                .or(() -> refreshTokenRepository.findByUsedHash(hash))
                .orElseThrow(() -> new RefreshTokenException(RefreshTokenProblem.NOT_FOUND));
    }

    private void rotate(RefreshToken stored, String token) {
        refreshTokenRepository.renew(stored.getId(), stored.getCurrentHash(), hash(token));
    }

    private static boolean isExpired(RefreshToken stored) {
        return !stored.getExpiresAt().isAfter(Instant.now());
    }
}
