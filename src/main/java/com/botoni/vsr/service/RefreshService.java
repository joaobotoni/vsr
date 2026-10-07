package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.RefreshToken;
import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.dto.request.RefreshRequest;
import com.botoni.vsr.dto.response.TokenResponse;
import com.botoni.vsr.exception.custom.RefreshTokenException;
import com.botoni.vsr.exception.enums.problem.RefreshTokenProblem;
import com.botoni.vsr.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshService {

    private final RefreshTokenService refreshTokenService;
    private final SessionService sessionService;
    private final TokenService tokenService;

    public TokenResponse refresh(RefreshRequest request) {
        RefreshToken stored = find(request);
        revokeIfReused(stored, request);
        return exchange(stored, request);
    }

    private RefreshToken find(RefreshRequest request) {
        return refreshTokenService.find(request.refreshToken());
    }

    private void revokeIfReused(RefreshToken stored, RefreshRequest request) {
        if (!isReused(stored, request)) {
            return;
        }
        revoke(stored);
    }

    private TokenResponse exchange(RefreshToken stored, RefreshRequest request) {
        if (isReused(stored, request)) {
            throw new RefreshTokenException(RefreshTokenProblem.REUSED);
        }
        return rotate(stored, findOwner(stored));
    }

    private boolean isReused(RefreshToken stored, RefreshRequest request) {
        return refreshTokenService.isReused(stored, request.refreshToken());
    }

    private void revoke(RefreshToken stored) {
        sessionService.revoke(findOwner(stored), session(stored));
    }

    private User findOwner(RefreshToken stored) {
        return sessionService.findOwner(session(stored));
    }

    private TokenResponse rotate(RefreshToken stored, User user) {
        access(user, stored);
        String refreshToken = renew(stored);
        return issue(user, stored, refreshToken);
    }

    private void access(User user, RefreshToken stored) {
        sessionService.access(user, session(stored));
    }

    private String renew(RefreshToken stored) {
        return refreshTokenService.renew(stored);
    }

    private TokenResponse issue(User user, RefreshToken stored, String refreshToken) {
        return tokenService.issue(Principal.from(user), session(stored), refreshToken);
    }

    private static Integer session(RefreshToken stored) {
        return stored.getId();
    }
}
