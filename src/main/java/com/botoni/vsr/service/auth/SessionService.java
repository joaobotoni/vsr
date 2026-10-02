package com.botoni.vsr.service.auth;

import com.botoni.vsr.configuration.properties.SessionProperties;
import com.botoni.vsr.dto.request.DeviceRequest;
import com.botoni.vsr.entity.users.Device;
import com.botoni.vsr.entity.users.Session;
import com.botoni.vsr.repository.DeviceRepository;
import com.botoni.vsr.repository.SessionRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class SessionService {

    private final DeviceRepository deviceRepository;
    private final SessionRepository sessionRepository;

    private final SessionProperties properties;

    public SessionService(DeviceRepository deviceRepository, SessionRepository sessionRepository, SessionProperties properties) {
        this.deviceRepository = deviceRepository;
        this.sessionRepository = sessionRepository;
        this.properties = properties;
    }


    private void save(DeviceRequest request) {

    }

    private Session open(Device device) {
        Session session = Session.builder()
                .device(device)
                .expiresAt(Instant.ofEpochMilli(properties.ttl().toMillis()))
                .build();

        return sessionRepository.save(session);
    }
}
