package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.RefreshToken;
import com.botoni.vsr.database.entity.Session;
import com.botoni.vsr.database.repository.RefreshTokenRepository;
import com.botoni.vsr.exception.custom.RefreshTokenException;
import com.botoni.vsr.exception.enums.problem.RefreshTokenProblem;
import com.botoni.vsr.token.OpaqueToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final OpaqueToken opaqueToken;

    @Transactional
    public String issue(Session session) {
        String refreshToken = generate();
        save(session, refreshToken);
        return refreshToken;
    }

    public byte[] hash(String refreshToken) {
        return opaqueToken.hash(refreshToken);
    }

    @Transactional(readOnly = true)
    public RefreshToken find(byte[] hash) {
        return refreshTokenRepository.findByCurrentHash(hash)
                .or(() -> refreshTokenRepository.findByUsedHash(hash))
                .orElseThrow(() -> new RefreshTokenException(RefreshTokenProblem.NOT_FOUND));
    }

    @Transactional
    public String renew(RefreshToken stored) {
        String refreshToken = generate();
        rotate(stored, refreshToken);
        return refreshToken;
    }

    public boolean isReused(RefreshToken stored, byte[] hash) {
        return !Arrays.equals(stored.getCurrentHash(), hash);
    }

    private String generate() {
        return opaqueToken.generate();
    }

    private void save(Session session, String refreshToken) {
        refreshTokenRepository.issue(session.getId(), hash(refreshToken));
    }

    private void rotate(RefreshToken stored, String refreshToken) {
        refreshTokenRepository.renew(stored.getId(), stored.getCurrentHash(), hash(refreshToken));
    }
}
