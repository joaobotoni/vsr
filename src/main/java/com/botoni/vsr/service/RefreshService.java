package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.RefreshToken;
import com.botoni.vsr.database.entity.Session;
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
        RefreshToken refreshToken = find(request);
        revokeIfReused(refreshToken, request);
        requireNotReused(refreshToken, request);
        access(refreshToken);
        String renewed = renew(refreshToken);
        return issue(refreshToken, renewed);
    }

    private RefreshToken find(RefreshRequest request) {
        return refreshTokenService.find(request.refreshToken());
    }

    private void revokeIfReused(RefreshToken refreshToken, RefreshRequest request) {
        if (isReused(refreshToken, request)) {
            revoke(refreshToken);
        }
    }

    private void requireNotReused(RefreshToken refreshToken, RefreshRequest request) {
        if (isReused(refreshToken, request)) {
            throw new RefreshTokenException(RefreshTokenProblem.REUSED);
        }
    }

    private boolean isReused(RefreshToken refreshToken, RefreshRequest request) {
        return refreshTokenService.isReused(refreshToken, request.refreshToken());
    }

    private void revoke(RefreshToken refreshToken) {
        sessionService.revoke(user(refreshToken), session(refreshToken).getId());
    }

    private void access(RefreshToken refreshToken) {
        sessionService.access(user(refreshToken), session(refreshToken).getId());
    }

    private String renew(RefreshToken refreshToken) {
        return refreshTokenService.renew(refreshToken);
    }

    private TokenResponse issue(RefreshToken refreshToken, String renewed) {
        return tokenService.issue(principal(refreshToken), session(refreshToken).getId(), renewed);
    }

    private static Principal principal(RefreshToken refreshToken) {
        return Principal.from(user(refreshToken));
    }

    private static Session session(RefreshToken refreshToken) {
        return refreshToken.getSession();
    }

    private static User user(RefreshToken refreshToken) {
        return session(refreshToken).getDevice().getUser();
    }
}
