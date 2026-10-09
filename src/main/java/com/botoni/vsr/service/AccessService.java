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
    public AuthenticationResponse authenticate(User user, DeviceRequest request, InetAddress ip) {
        Device device = register(user, request);
        Session session = open(device, ip);
        String refreshToken = issue(session);
        TokenResponse token = sign(user, session, refreshToken);
        return show(user, token);
    }

    private Device register(User user, DeviceRequest request) {
        return deviceService.register(user, request);
    }

    private Session open(Device device, InetAddress ip) {
        return sessionService.open(device, ip);
    }

    private String issue(Session session) {
        return refreshTokenService.issue(session);
    }

    private TokenResponse sign(User user, Session session, String refreshToken) {
        return tokenService.issue(user, session.getId(), refreshToken);
    }

    private AuthenticationResponse show(User user, TokenResponse token) {
        return authenticationMapper.response(user, token);
    }
}