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
        save(session, token);
        return token;
    }

    @Transactional(readOnly = true)
    public RefreshToken find(String token) {
        RefreshToken found = findByHash(hash(token));
        requireNotExpired(found);
        return found;
    }

    @Transactional
    public String renew(RefreshToken refreshToken) {
        String token = generate();
        rotate(refreshToken, token);
        return token;
    }

    public boolean isReused(RefreshToken refreshToken, String token) {
        return Arrays.equals(refreshToken.getPreviousHash(), hash(token));
    }

    private void save(Session session, String token) {
        refreshTokenRepository.issue(session.getId(), hash(token));
    }

    private void rotate(RefreshToken refreshToken, String token) {
        refreshTokenRepository.renew(refreshToken.getId(), hash(token));
    }

    private RefreshToken findByHash(byte[] hash) {
        return refreshTokenRepository.findByCurrentHash(hash)
                .or(() -> refreshTokenRepository.findByPreviousHash(hash))
                .orElseThrow(() -> new RefreshTokenException(RefreshTokenProblem.NOT_FOUND));
    }

    private String generate() {
        return opaqueToken.generate();
    }

    private byte[] hash(String token) {
        return opaqueToken.hash(token);
    }

    private static void requireNotExpired(RefreshToken refreshToken) {
        if (isExpired(refreshToken)) {
            throw new RefreshTokenException(RefreshTokenProblem.EXPIRED);
        }
    }

    private static boolean isExpired(RefreshToken refreshToken) {
        return !refreshToken.getExpiresAt().isAfter(Instant.now());
    }
}
