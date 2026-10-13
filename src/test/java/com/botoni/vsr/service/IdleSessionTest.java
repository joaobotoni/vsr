package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.Device;
import com.botoni.vsr.database.entity.Session;
import com.botoni.vsr.database.repository.SessionRepository;
import com.botoni.vsr.exception.custom.SessionException;
import com.botoni.vsr.exception.enums.problem.SessionProblem;
import com.botoni.vsr.mapper.SessionMapper;
import com.botoni.vsr.properties.SessionProperties;
import com.botoni.vsr.support.Controle;
import com.botoni.vsr.support.Users;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Sessão ociosa")
class IdleSessionTest {

    private static final int SESSION = 10;
    private static final Duration TTL = Duration.ofDays(30);
    private static final Duration IDLE = Duration.ofDays(7);
    private static final int MAX = 10;

    private final SessionRepository repository = mock(SessionRepository.class);
    private final SessionService sessionService = new SessionService(
            repository, Mappers.getMapper(SessionMapper.class), new SessionProperties(TTL, IDLE, MAX), Clock.systemUTC());

    @Test
    @Controle
    @DisplayName("sessão parada há 29 dias não renova tokens, mesmo antes do fim da validade absoluta")
    void longIdleSessionIsNotResumed() {
        Device device = Device.builder().id(3).user(Users.ana()).build();
        Session idle = Session.builder().id(SESSION).device(device)
                .lastAccessAt(Instant.now().minus(Duration.ofDays(29)))
                .expiresAt(Instant.now().plus(Duration.ofDays(1))).build();
        when(repository.findWithUserById(SESSION)).thenReturn(Optional.of(idle));

        assertThatThrownBy(() -> sessionService.resume(SESSION))
                .isInstanceOf(SessionException.class)
                .extracting("problem").isEqualTo(SessionProblem.IDLE);
        verify(repository, never()).access(anyInt());
    }
}
