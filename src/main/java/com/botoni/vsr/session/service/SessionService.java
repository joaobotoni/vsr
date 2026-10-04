package com.botoni.vsr.session.service;

import com.botoni.vsr.infra.configuration.properties.SessionProperties;
import com.botoni.vsr.bundle.SessionOwnerBundle;
import com.botoni.vsr.session.entity.Device;
import com.botoni.vsr.session.entity.Session;
import com.botoni.vsr.session.mapper.SessionMapper;
import com.botoni.vsr.session.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final DeviceService deviceService;
    private final SessionRepository sessionRepository;
    private final SessionMapper sessionMapper;
    private final SessionProperties properties;

    @Transactional
    public Session open(SessionOwnerBundle bundle) {
        Device device = device(bundle);
        return start(device, bundle);
    }

    private Device device(SessionOwnerBundle bundle) {
        return deviceService.upsert(bundle.user(), bundle.session().device());
    }

    private Session start(Device device, SessionOwnerBundle bundle) {
        return sessionRepository.save(sessionMapper.toEntity(device, bundle, expiration()));
    }

    private Instant expiration() {
        return Instant.now().plus(properties.ttl());
    }
}
