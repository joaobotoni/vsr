package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.Device;
import com.botoni.vsr.database.entity.Session;
import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.database.repository.SessionRepository;
import com.botoni.vsr.exception.custom.SessionException;
import com.botoni.vsr.exception.enums.problem.SessionProblem;
import com.botoni.vsr.mapper.SessionMapper;
import com.botoni.vsr.properties.SessionProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {

    private static final Integer KEEP_NONE = null;

    private final Clock clock;
    private final SessionRepository sessionRepository;
    private final SessionMapper sessionMapper;
    private final SessionProperties sessionProperties;
    private final SessionPolicyService sessionPolicyService;

    @Transactional
    public Session open(Device device, InetAddress ip) {
        clear(device);
        Session session = save(create(device, ip));
        cap(device);
        return session;
    }

    @Transactional
    public void access(UUID user, Integer session) {
        use(owned(user, session));
    }

    @Transactional
    public User resume(Integer session) {
        Session found = find(session);
        use(found);
        return owner(found);
    }

    @Transactional
    public void revoke(UUID user, Integer session) {
        close(owned(user, session));
    }

    @Transactional
    public void invalidate(Integer session) {
        sessionRepository.revoke(session);
    }

    @Transactional
    public Device revokeAll(UUID user, Integer session) {
        Session ended = owned(user, session);
        active(ended);
        dismiss(owner(ended));
        return ended.getDevice();
    }

    private void clear(Device device) {
        sessionRepository.revokeDevice(device.getId());
    }

    private Session create(Device device, InetAddress ip) {
        return sessionMapper.entity(device, ip, expiration());
    }

    private Instant expiration() {
        return Instant.now(clock).plus(sessionProperties.ttl());
    }

    private Session save(Session session) {
        return sessionRepository.save(session);
    }

    private void cap(Device device) {
        sessionRepository.limitUser(device.getUser().getId(), sessionProperties.max());
    }

    private void use(Session session) {
        active(session);
        touch(session);
    }

    private void active(Session session) {
        sessionPolicyService.active(session);
    }

    private void touch(Session session) {
        sessionRepository.access(session.getId());
    }

    private static User owner(Session session) {
        return session.getDevice().getUser();
    }

    private void close(Session session) {
        sessionRepository.revoke(session.getId());
    }

    private void dismiss(User user) {
        sessionRepository.revokeUser(user.getId(), KEEP_NONE);
    }

    private Session owned(UUID user, Integer session) {
        return sessionRepository.findWithUserByIdAndUuid(session, user)
                .orElseThrow(() -> new SessionException(SessionProblem.NOT_FOUND));
    }

    private Session find(Integer session) {
        return sessionRepository.findWithUserById(session)
                .orElseThrow(() -> new SessionException(SessionProblem.NOT_FOUND));
    }
}
