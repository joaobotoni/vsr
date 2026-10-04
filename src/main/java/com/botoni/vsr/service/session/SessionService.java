package com.botoni.vsr.service.session;

import com.botoni.vsr.properties.security.SessionProperties;
import com.botoni.vsr.command.session.SessionOwnerCommand;
import com.botoni.vsr.entity.users.Device;
import com.botoni.vsr.entity.users.Session;
import com.botoni.vsr.mapper.session.SessionMapper;
import com.botoni.vsr.repository.SessionRepository;
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
    public Session open(SessionOwnerCommand command) {
        Device device = device(command);
        return start(device, command);
    }

    private Device device(SessionOwnerCommand command) {
        return deviceService.upsert(command.user(), command.session().device());
    }

    private Session start(Device device, SessionOwnerCommand command) {
        return sessionRepository.save(sessionMapper.toEntity(device, command, expiration()));
    }

    private Instant expiration() {
        return Instant.now().plus(properties.ttl());
    }
}
