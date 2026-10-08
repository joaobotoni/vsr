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
import java.util.UUID;

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
    public void access(UUID user, Integer session) {
        Session active = active(findOwned(user, session));
        registerAccess(active);
    }

    @Transactional
    public User resume(Integer session) {
        Session active = active(find(session));
        registerAccess(active);
        return owner(active);
    }

    @Transactional
    public void revoke(UUID user, Integer session) {
        revokeSession(findOwned(user, session));
    }

    @Transactional
    public void terminate(Integer session) {
        sessionRepository.revoke(session);
    }

    @Transactional
    public void revokeOthers(UUID user, Integer session) {
        Session kept = active(findOwned(user, session));
        revokeExcept(kept);
    }

    private void revokeDevice(Device device) {
        sessionRepository.revokeDevice(device.getId());
    }

    private Session create(Device device, InetAddress ip) {
        return sessionMapper.toEntity(device, ip, expiration());
    }

    private Instant expiration() {
        return Instant.now().plus(sessionProperties.ttl());
    }

    private Session persist(Session session) {
        return sessionRepository.save(session);
    }

    private Session findOwned(UUID user, Integer session) {
        return sessionRepository.findWithUserByIdAndUuid(session, user)
                .orElseThrow(() -> new SessionException(SessionProblem.NOT_FOUND));
    }

    private void registerAccess(Session session) {
        sessionRepository.access(session.getId());
    }

    private Session find(Integer session) {
        return sessionRepository.findWithUserById(session)
                .orElseThrow(() -> new SessionException(SessionProblem.NOT_FOUND));
    }

    private void revokeSession(Session session) {
        sessionRepository.revoke(session.getId());
    }

    private void revokeExcept(Session kept) {
        sessionRepository.revokeUser(owner(kept).getId(), kept.getId());
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
