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
    private final SessionProperties sessionProperties;

    @Transactional
    public Session open(Device device, InetAddress ip) {
        revokeDevice(device);
        Session session = create(device, ip);
        return persist(session);
    }

    @Transactional
    public void access(User user, Integer session) {
        Session active = findActive(user, session);
        registerAccess(active);
    }

    @Transactional
    public void revoke(User user, Integer session) {
        Session found = find(user, session);
        revokeSession(found);
    }

    @Transactional
    public void revokeOthers(User user, Integer keptSession) {
        Session kept = findActive(user, keptSession);
        revokeExcept(user, kept);
    }

    @Transactional(readOnly = true)
    public User findOwner(Integer session) {
        Session found = findWithUser(session);
        return owner(found);
    }

    private void revokeDevice(Device device) {
        sessionRepository.revokeDevice(device.getId());
    }

    private Session create(Device device, InetAddress ip) {
        return sessionMapper.toEntity(device, ip, expiration());
    }

    private Session persist(Session session) {
        return sessionRepository.save(session);
    }

    private Session findActive(User user, Integer session) {
        return active(find(user, session));
    }

    private void registerAccess(Session session) {
        sessionRepository.access(session.getId());
    }

    private Session find(User user, Integer session) {
        return sessionRepository.findByIdAndDeviceUser(session, user)
                .orElseThrow(() -> new SessionException(SessionProblem.NOT_FOUND));
    }

    private void revokeSession(Session session) {
        sessionRepository.revoke(session.getId());
    }

    private void revokeExcept(User user, Session kept) {
        sessionRepository.revokeUser(user.getId(), kept.getId());
    }

    private Session findWithUser(Integer session) {
        return sessionRepository.findWithUserById(session)
                .orElseThrow(() -> new SessionException(SessionProblem.NOT_FOUND));
    }

    private Instant expiration() {
        return Instant.now().plus(sessionProperties.ttl());
    }

    private static Session active(Session session) {
        if (isRevoked(session)) {
            throw new SessionException(SessionProblem.REVOKED);
        }
        if (isExpired(session)) {
            throw new SessionException(SessionProblem.EXPIRED);
        }
        return session;
    }

    private static User owner(Session session) {
        return session.getDevice().getUser();
    }

    private static boolean isRevoked(Session session) {
        return session.getRevokedAt() != null;
    }

    private static boolean isExpired(Session session) {
        return !session.getExpiresAt().isAfter(Instant.now());
    }
}
