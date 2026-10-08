package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.Device;
import com.botoni.vsr.database.entity.Session;
import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.dto.request.DeviceRequest;
import com.botoni.vsr.dto.response.AuthenticationResponse;
import com.botoni.vsr.dto.response.TokenResponse;
import com.botoni.vsr.mapper.AuthenticationMapper;
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
    private final AuthenticationMapper authenticationMapper;

    @Transactional
    public AuthenticationResponse grant(User user, DeviceRequest request, InetAddress ip) {
        Device device = registerDevice(user, request);
        Session session = open(device, ip);
        String refreshToken = issueRefreshToken(session);
        TokenResponse token = issue(user, session, refreshToken);
        return respond(user, token);
    }

    private Device registerDevice(User user, DeviceRequest request) {
        return deviceService.register(user, request);
    }

    private Session open(Device device, InetAddress ip) {
        return sessionService.open(device, ip);
    }

    private String issueRefreshToken(Session session) {
        return refreshTokenService.issue(session);
    }

    private TokenResponse issue(User user, Session session, String refreshToken) {
        return tokenService.issue(user, session.getId(), refreshToken);
    }

    private AuthenticationResponse respond(User user, TokenResponse token) {
        return authenticationMapper.toResponse(user, token);
    }
}
