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
        byte[] digest = digest(request);
        RefreshToken stored = find(digest);
        terminateIfReused(stored, digest);
        return exchange(stored, digest);
    }

    private byte[] digest(RefreshRequest request) {
        return refreshTokenService.digest(request.refreshToken());
    }

    private RefreshToken find(byte[] digest) {
        return refreshTokenService.find(digest);
    }

    private void terminateIfReused(RefreshToken stored, byte[] digest) {
        if (!isReused(stored, digest)) {
            return;
        }
        terminate(stored);
    }

    private TokenResponse exchange(RefreshToken stored, byte[] digest) {
        if (isReused(stored, digest)) {
            throw new RefreshTokenException(RefreshTokenProblem.REUSED);
        }
        return rotate(stored);
    }

    private boolean isReused(RefreshToken stored, byte[] digest) {
        return refreshTokenService.isReused(stored, digest);
    }

    private void terminate(RefreshToken stored) {
        sessionService.terminate(session(stored));
    }

    private TokenResponse rotate(RefreshToken stored) {
        User user = resume(stored);
        String refreshToken = renew(stored);
        return issue(user, stored, refreshToken);
    }

    private User resume(RefreshToken stored) {
        return sessionService.resume(session(stored));
    }

    private String renew(RefreshToken stored) {
        return refreshTokenService.renew(stored);
    }

    private TokenResponse issue(User user, RefreshToken stored, String refreshToken) {
        return tokenService.issue(user, session(stored), refreshToken);
    }

    private static Integer session(RefreshToken stored) {
        return stored.getId();
    }
}
