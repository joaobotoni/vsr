package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.Session;
import com.botoni.vsr.exception.custom.SessionException;
import com.botoni.vsr.exception.enums.problem.SessionProblem;
import com.botoni.vsr.properties.SessionProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SessionPolicyService {

    private final SessionProperties sessionProperties;
    private final Clock clock;

    public void active(Session session) {
        unrevoked(session);
        unexpired(session);
        recent(session);
    }

    private Instant now() {
        return Instant.now(clock);
    }

    private Instant cutoff() {
        return now().minus(sessionProperties.idle());
    }

    private void unrevoked(Session session) {
        if (revoked(session)) {
            throw new SessionException(SessionProblem.REVOKED);
        }
    }

    private void unexpired(Session session) {
        if (expired(session)) {
            throw new SessionException(SessionProblem.EXPIRED);
        }
    }

    private void recent(Session session) {
        if (idle(session)) {
            throw new SessionException(SessionProblem.IDLE);
        }
    }

    private static boolean revoked(Session session) {
        return session.getRevokedAt() != null;
    }

    private boolean expired(Session session) {
        return !session.getExpiresAt().isAfter(now());
    }

    private boolean idle(Session session) {
        return !session.getLastAccessAt().isAfter(cutoff());
    }
}
