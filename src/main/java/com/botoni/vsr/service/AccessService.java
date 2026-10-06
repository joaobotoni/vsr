package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.Device;
import com.botoni.vsr.database.entity.Session;
import com.botoni.vsr.dto.request.DeviceRequest;
import com.botoni.vsr.dto.response.TokenResponse;
import com.botoni.vsr.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;

@Service
@RequiredArgsConstructor
public class AccessService {

    private final DeviceService deviceService;
    private final SessionService sessionService;
    private final RefreshTokenService refreshTokenService;
    private final TokenService tokenService;

    @Transactional
    public TokenResponse grant(Principal principal, DeviceRequest request, InetAddress ip) {
        Device device = register(principal, request);
        Session session = open(device, ip);
        String refreshToken = issueRefresh(session);
        return issue(principal, session, refreshToken);
    }

    private Device register(Principal principal, DeviceRequest request) {
        return deviceService.upsert(principal.user(), request);
    }

    private Session open(Device device, InetAddress ip) {
        return sessionService.open(device, ip);
    }

    private String issueRefresh(Session session) {
        return refreshTokenService.issue(session);
    }

    private TokenResponse issue(Principal principal, Session session, String refreshToken) {
        return tokenService.issue(principal, session.getId(), refreshToken);
    }
}
