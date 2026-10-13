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

    private final SessionRepository sessionRepository;
    private final SessionMapper sessionMapper;
    private final SessionProperties sessionProperties;
    private final Clock clock;

    @Transactional
    public Session open(Device device, InetAddress ip) {
        reset(device);
        Session session = save(create(device, ip));
        limit(device);
        return session;
    }

    @Transactional
    public void access(UUID user, Integer session) {
        Session found = owned(user, session);
        active(found);
        touch(found);
    }

    @Transactional
    public User resume(Integer session) {
        Session found = find(session);
        active(found);
        touch(found);
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
    public Device end(UUID user, Integer session) {
        Session ended = owned(user, session);
        active(ended);
        dismiss(ended);
        return ended.getDevice();
    }

    private void reset(Device device) {
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

    private void limit(Device device) {
        sessionRepository.limitUser(device.getUser().getId(), sessionProperties.max());
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

    private void dismiss(Session ended) {
        sessionRepository.revokeUser(owner(ended).getId(), null);
    }

    private Session owned(UUID user, Integer session) {
        return sessionRepository.findWithUserByIdAndUuid(session, user)
                .orElseThrow(() -> new SessionException(SessionProblem.NOT_FOUND));
    }

    private Session find(Integer session) {
        return sessionRepository.findWithUserById(session)
                .orElseThrow(() -> new SessionException(SessionProblem.NOT_FOUND));
    }

    private void active(Session session) {
        revoked(session);
        expired(session);
        idle(session);
    }

    private void revoked(Session session) {
        if (isRevoked(session)) {
            throw new SessionException(SessionProblem.REVOKED);
        }
    }

    private void expired(Session session) {
        if (isExpired(session)) {
            throw new SessionException(SessionProblem.EXPIRED);
        }
    }

    private void idle(Session session) {
        if (isIdle(session)) {
            throw new SessionException(SessionProblem.IDLE);
        }
    }

    private static boolean isRevoked(Session session) {
        return session.getRevokedAt() != null;
    }

    private boolean isExpired(Session session) {
        return !session.getExpiresAt().isAfter(Instant.now(clock));
    }

    private boolean isIdle(Session session) {
        return !session.getLastAccessAt().isAfter(Instant.now(clock)
                .minus(sessionProperties.idle()));
    }
}
