package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.Device;
import com.botoni.vsr.database.entity.LocalCredential;
import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.dto.response.TokenResponse;
import com.botoni.vsr.exception.custom.SessionException;
import com.botoni.vsr.exception.enums.problem.SessionProblem;
import com.botoni.vsr.support.Controle;
import com.botoni.vsr.support.Users;
import com.botoni.vsr.vo.PasswordHash;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Senha")
class PasswordServiceTest {

    private static final int SESSION = 10;
    private static final PasswordHash HASH = PasswordHash.of("$argon2id$v=19$m=19456,t=2,p=1$c2FsdA$aGFzaA");

    private final LocalCredentialService localCredentialService = mock(LocalCredentialService.class);
    private final SessionService sessionService = mock(SessionService.class);
    private final AccessService accessService = mock(AccessService.class);
    private final PasswordService passwordService = new PasswordService(localCredentialService, sessionService, accessService);

    private final User user = Users.ana();
    private final Device device = Device.builder().id(3).user(user).build();
    private final LocalCredential credential = LocalCredential.builder().id(user.getId()).user(user).build();
    private final InetAddress ip = InetAddress.getLoopbackAddress();

    @BeforeEach
    void currentSession() {
        when(sessionService.revokeAll(Users.UUID, SESSION)).thenReturn(device);
    }

    @Test
    @Controle
    @DisplayName("substituir a senha encerra todas as sessões, grava o hash e abre uma sessão nova no mesmo dispositivo")
    void replaceEndsSessionsThenChangesPasswordThenRotates() {
        TokenResponse token = new TokenResponse("access", "refresh", 900);
        when(accessService.rotate(device, ip)).thenReturn(token);

        assertThat(passwordService.commit(Users.UUID, SESSION, credential, HASH, ip)).isEqualTo(token);

        InOrder order = inOrder(sessionService, localCredentialService, accessService);
        order.verify(sessionService).revokeAll(Users.UUID, SESSION);
        order.verify(localCredentialService).replace(credential, HASH);
        order.verify(accessService).rotate(device, ip);
    }

    @Test
    @Controle
    @DisplayName("sessão atual inativa falha antes de gravar a senha ou abrir sessão")
    void inactiveSessionDoesNotChangePassword() {
        doThrow(new SessionException(SessionProblem.REVOKED)).when(sessionService).revokeAll(Users.UUID, SESSION);

        assertThatThrownBy(() -> passwordService.commit(Users.UUID, SESSION, credential, HASH, ip))
                .isInstanceOf(SessionException.class);
        verify(localCredentialService, never()).replace(any(), any());
        verify(accessService, never()).rotate(any(), any());
    }

    @Test
    @Controle
    @DisplayName("troca concorrente detectada no banco falha e desfaz a transação inteira")
    void concurrentReplaceFails() throws Exception {
        doThrow(new DataIntegrityViolationException("rn_senha_alterada"))
                .when(localCredentialService).replace(credential, HASH);

        assertThatThrownBy(() -> passwordService.commit(Users.UUID, SESSION, credential, HASH, ip))
                .isInstanceOf(DataIntegrityViolationException.class);
        verify(accessService, never()).rotate(any(), any());
        assertThat(PasswordService.class.getMethod("commit", UUID.class, Integer.class, LocalCredential.class,
                PasswordHash.class, InetAddress.class).isAnnotationPresent(Transactional.class)).isTrue();
    }
}
