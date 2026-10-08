package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.Device;
import com.botoni.vsr.database.entity.Session;
import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.database.repository.SessionRepository;
import com.botoni.vsr.exception.custom.SessionException;
import com.botoni.vsr.exception.enums.problem.SessionProblem;
import com.botoni.vsr.mapper.SessionMapper;
import com.botoni.vsr.properties.SessionProperties;
import com.botoni.vsr.support.Controle;
import com.botoni.vsr.support.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.InOrder;

import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Sessões")
class SessionServiceTest {

    private static final int SESSION = 10;
    private static final Duration TTL = Duration.ofDays(30);

    private final SessionRepository repository = mock(SessionRepository.class);
    private final SessionService sessionService = new SessionService(
            repository, Mappers.getMapper(SessionMapper.class), new SessionProperties(TTL));

    private final User owner = Users.ana();
    private final Device device = Device.builder().id(3).user(owner).build();
    private Session session;

    @BeforeEach
    void activeSession() {
        session = Session.builder().id(SESSION).device(device).expiresAt(Instant.now().plus(TTL)).build();
        when(repository.findWithUserByIdAndUuid(any(), any())).thenReturn(Optional.empty());
        when(repository.findWithUserByIdAndUuid(SESSION, Users.UUID)).thenReturn(Optional.of(session));
        when(repository.findWithUserById(SESSION)).thenReturn(Optional.of(session));
    }

    @Test
    @Controle
    @DisplayName("abrir sessão revoga as anteriores do dispositivo antes de gravar a nova")
    void openRevokesDeviceSessionsFirst() {
        when(repository.save(any())).then(returnsFirstArg());

        Session opened = sessionService.open(device, InetAddress.getLoopbackAddress());

        InOrder order = inOrder(repository);
        order.verify(repository).revokeDevice(device.getId());
        order.verify(repository).save(any());
        assertThat(opened.getDevice()).isSameAs(device);
        assertThat(opened.getExpiresAt()).isCloseTo(Instant.now().plus(TTL), within(Duration.ofSeconds(5)));
    }

    @Test
    @Controle
    @DisplayName("acesso a sessão ativa registra o último acesso")
    void activeSessionRegistersAccess() {
        sessionService.access(Users.UUID, SESSION);

        verify(repository).access(SESSION);
    }

    @Test
    @Controle
    @DisplayName("sessão revogada é recusada sem registrar acesso")
    void revokedSessionIsRejected() {
        session.setRevokedAt(Instant.now());

        assertThatThrownBy(() -> sessionService.access(Users.UUID, SESSION))
                .isInstanceOf(SessionException.class)
                .extracting("problem").isEqualTo(SessionProblem.REVOKED);
        verify(repository, never()).access(anyInt());
    }

    @Test
    @Controle
    @DisplayName("sessão expirada é recusada sem registrar acesso")
    void expiredSessionIsRejected() {
        session.setExpiresAt(Instant.now().minusSeconds(1));

        assertThatThrownBy(() -> sessionService.access(Users.UUID, SESSION))
                .isInstanceOf(SessionException.class)
                .extracting("problem").isEqualTo(SessionProblem.EXPIRED);
        verify(repository, never()).access(anyInt());
    }

    @Test
    @Controle
    @DisplayName("sessão de outro usuário é tratada como inexistente")
    void otherUsersSessionIsNotFound() {
        assertThatThrownBy(() -> sessionService.access(UUID.randomUUID(), SESSION))
                .isInstanceOf(SessionException.class)
                .extracting("problem").isEqualTo(SessionProblem.NOT_FOUND);
        verify(repository, never()).access(anyInt());
    }

    @Test
    @Controle
    @DisplayName("logout revoga a sessão do próprio usuário")
    void revokeRevokesOwnSession() {
        sessionService.revoke(Users.UUID, SESSION);

        verify(repository).revoke(SESSION);
    }

    @Test
    @Controle
    @DisplayName("logout com a sessão de outro usuário não revoga nada")
    void revokeOfOtherUsersSessionDoesNothing() {
        assertThatThrownBy(() -> sessionService.revoke(UUID.randomUUID(), SESSION))
                .isInstanceOf(SessionException.class)
                .extracting("problem").isEqualTo(SessionProblem.NOT_FOUND);
        verify(repository, never()).revoke(anyInt());
    }

    @Test
    @Controle
    @DisplayName("revogar as outras sessões mantém a atual e usa o id interno do dono")
    void revokeOthersKeepsCurrentSession() {
        sessionService.revokeOthers(Users.UUID, SESSION);

        verify(repository).revokeUser(owner.getId(), SESSION);
    }

    @Test
    @Controle
    @DisplayName("revogar as outras sessões a partir de uma sessão revogada é recusado")
    void revokeOthersFromRevokedSessionIsRejected() {
        session.setRevokedAt(Instant.now());

        assertThatThrownBy(() -> sessionService.revokeOthers(Users.UUID, SESSION))
                .isInstanceOf(SessionException.class)
                .extracting("problem").isEqualTo(SessionProblem.REVOKED);
        verify(repository, never()).revokeUser(anyInt(), anyInt());
    }

    @Test
    @Controle
    @DisplayName("retomar uma sessão ativa registra o acesso e devolve o dono, com uma única busca")
    void resumeRegistersAccessAndReturnsOwner() {
        assertThat(sessionService.resume(SESSION)).isSameAs(owner);

        verify(repository).findWithUserById(SESSION);
        verify(repository).access(SESSION);
        verify(repository, never()).findWithUserByIdAndUuid(any(), any());
    }

    @Test
    @Controle
    @DisplayName("sessão revogada não é retomada")
    void revokedSessionIsNotResumed() {
        session.setRevokedAt(Instant.now());

        assertThatThrownBy(() -> sessionService.resume(SESSION))
                .isInstanceOf(SessionException.class)
                .extracting("problem").isEqualTo(SessionProblem.REVOKED);
        verify(repository, never()).access(anyInt());
    }

    @Test
    @Controle
    @DisplayName("sessão inexistente não é retomada")
    void unknownSessionIsNotResumed() {
        assertThatThrownBy(() -> sessionService.resume(99))
                .isInstanceOf(SessionException.class)
                .extracting("problem").isEqualTo(SessionProblem.NOT_FOUND);
    }

    @Test
    @Controle
    @DisplayName("encerrar por reuso revoga direto pelo id, sem buscar a sessão")
    void terminateRevokesWithoutLookup() {
        sessionService.terminate(SESSION);

        verify(repository).revoke(SESSION);
        verify(repository, never()).findWithUserById(any());
    }
}
