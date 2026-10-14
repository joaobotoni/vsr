package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.RefreshToken;
import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.dto.request.RefreshRequest;
import com.botoni.vsr.dto.response.TokenResponse;
import com.botoni.vsr.exception.custom.RefreshTokenException;
import com.botoni.vsr.exception.enums.problem.RefreshTokenProblem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshService {

    private final RefreshTokenService refreshTokenService;
    private final SessionService sessionService;
    private final TokenService tokenService;

    public TokenResponse refresh(RefreshRequest request) {
        byte[] hash = hash(request);
        RefreshToken stored = find(hash);
        revokeReused(stored, hash);
        return rotate(stored, hash);
    }

    private byte[] hash(RefreshRequest request) {
        return refreshTokenService.hash(request.refreshToken());
    }

    private RefreshToken find(byte[] hash) {
        return refreshTokenService.find(hash);
    }

    private TokenResponse rotate(RefreshToken stored, byte[] hash) {
        fresh(stored, hash);
        User user = resume(stored);
        String refreshToken = renew(stored);
        return issue(user, stored, refreshToken);
    }

    private User resume(RefreshToken stored) {
        return sessionService.resume(session(stored));
    }

    private static Integer session(RefreshToken stored) {
        return stored.getId();
    }

    private void invalidate(RefreshToken stored) {
        sessionService.invalidate(session(stored));
    }

    private TokenResponse issue(User user, RefreshToken stored, String refreshToken) {
        return tokenService.issue(user, session(stored), refreshToken);
    }

    private void revokeReused(RefreshToken stored, byte[] hash) {
        if (!reused(stored, hash)) {
            return;
        }
        invalidate(stored);
    }

    private void fresh(RefreshToken stored, byte[] hash) {
        if (reused(stored, hash)) {
            throw new RefreshTokenException(RefreshTokenProblem.REUSED);
        }
    }

    private String renew(RefreshToken stored) {
        try {
            return refreshTokenService.renew(stored);
        } catch (RefreshTokenException exception) {
            invalidate(stored);
            throw exception;
        }
    }

    private boolean reused(RefreshToken stored, byte[] hash) {
        return refreshTokenService.reused(stored, hash);
    }
}
