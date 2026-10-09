package com.botoni.vsr.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LogoutService {

    private final SessionService sessionService;

    public void logout(UUID user, Integer session) {
        sessionService.revoke(user, session);
    }
}