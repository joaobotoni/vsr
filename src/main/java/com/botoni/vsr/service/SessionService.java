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
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final SessionMapper sessionMapper;
    private final SessionProperties properties;

    @Transactional
    public Session open(Device device, InetAddress ip) {
        revokeDevice(device);
        return save(device, ip);
    }

    @Transactional
    public void access(User user, Integer session) {
        Session active = findActive(user, session);
        registerAccess(active);
    }

    @Transactional
    public void revoke(User user, Integer session) {
        Session found = find(user, session);
        revoke(found);
    }

    @Transactional
    public void revokeUser(User user, Integer keptSession) {
        Session kept = findActive(user, keptSession);
        revokeOthers(user, kept);
    }

    private Session save(Device device, InetAddress ip) {
        return sessionRepository.save(create(device, ip));
    }

    private void revoke(Session session) {
        sessionRepository.revoke(session.getId());
    }

    private void revokeDevice(Device device) {
        sessionRepository.revokeDevice(device.getId());
    }

    private Session create(Device device, InetAddress ip) {
        return sessionMapper.toEntity(device, ip, expiration());
    }

    private void registerAccess(Session session) {
        sessionRepository.access(session.getId());
    }

    private void revokeOthers(User user, Session kept) {
        sessionRepository.revokeUser(user.getId(), kept.getId());
    }

    private Session findActive(User user, Integer session) {
        Session found = find(user, session);
        requireActive(found);
        return found;
    }

    private void requireActive(Session session) {
        requireNotRevoked(session);
        requireNotExpired(session);
    }

    private Session find(User user, Integer session) {
        return sessionRepository.findByIdAndDeviceUser(session, user)
                .orElseThrow(() -> new SessionException(SessionProblem.NOT_FOUND));
    }

    private Instant expiration() {
        return Instant.now().plus(properties.ttl());
    }

    private static void requireNotRevoked(Session session) {
        if (isRevoked(session)) {
            throw new SessionException(SessionProblem.REVOKED);
        }
    }

    private static void requireNotExpired(Session session) {
        if (isExpired(session)) {
            throw new SessionException(SessionProblem.EXPIRED);
        }
    }

    private static boolean isRevoked(Session session) {
        return session.getRevokedAt() != null;
    }

    private static boolean isExpired(Session session) {
        return !session.getExpiresAt().isAfter(Instant.now());
    }
}
