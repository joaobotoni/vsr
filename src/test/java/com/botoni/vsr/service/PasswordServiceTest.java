package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.LocalCredential;
import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.exception.custom.SessionException;
import com.botoni.vsr.exception.enums.problem.SessionProblem;
import com.botoni.vsr.support.Controle;
import com.botoni.vsr.support.Users;
import com.botoni.vsr.vo.PasswordHash;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@DisplayName("Senha")
class PasswordServiceTest {

    private static final int SESSION = 10;
    private static final PasswordHash HASH = PasswordHash.of("$argon2id$v=19$m=19456,t=2,p=1$c2FsdA$aGFzaA");

    private final LocalCredentialService localCredentialService = mock(LocalCredentialService.class);
    private final SessionService sessionService = mock(SessionService.class);
    private final PasswordService passwordService = new PasswordService(localCredentialService, sessionService);

    private final User user = Users.ana();
    private final LocalCredential credential = LocalCredential.builder().id(user.getId()).user(user).build();

    @Test
    @Controle
    @DisplayName("substituir a senha valida a sessão revogando as outras e só depois grava o hash")
    void replaceRevokesOthersThenChangesPassword() {
        passwordService.replace(Users.UUID, SESSION, credential, HASH);

        InOrder order = inOrder(sessionService, localCredentialService);
        order.verify(sessionService).keep(Users.UUID, SESSION);
        order.verify(localCredentialService).replace(credential, HASH);
    }

    @Test
    @Controle
    @DisplayName("sessão atual inativa falha antes de gravar a senha")
    void inactiveSessionDoesNotChangePassword() {
        doThrow(new SessionException(SessionProblem.REVOKED))
                .when(sessionService).keep(Users.UUID, SESSION);

        assertThatThrownBy(() -> passwordService.replace(Users.UUID, SESSION, credential, HASH))
                .isInstanceOf(SessionException.class);
        verify(localCredentialService, never()).replace(any(), any());
    }

    @Test
    @Controle
    @DisplayName("troca concorrente detectada no banco falha e desfaz a transação inteira")
    void concurrentReplaceFails() throws Exception {
        doThrow(new DataIntegrityViolationException("rn_senha_alterada"))
                .when(localCredentialService).replace(credential, HASH);

        assertThatThrownBy(() -> passwordService.replace(Users.UUID, SESSION, credential, HASH))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(PasswordService.class.getMethod("replace", UUID.class, Integer.class, LocalCredential.class, PasswordHash.class)
                .isAnnotationPresent(Transactional.class)).isTrue();
    }
}
