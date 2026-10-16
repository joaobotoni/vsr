package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.LocalCredential;
import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.database.repository.LocalCredentialRepository;
import com.botoni.vsr.exception.custom.CredentialException;
import com.botoni.vsr.exception.custom.SessionException;
import com.botoni.vsr.exception.enums.problem.CredentialProblem;
import com.botoni.vsr.exception.enums.problem.SessionProblem;
import com.botoni.vsr.mapper.LocalCredentialMapper;
import com.botoni.vsr.ratelimit.TokenBucket.Limit;
import com.botoni.vsr.ratelimit.RateLimitPolicy;
import com.botoni.vsr.support.Policies;
import com.botoni.vsr.support.Controle;
import com.botoni.vsr.support.Users;
import com.botoni.vsr.vo.Password;
import com.botoni.vsr.vo.PasswordHash;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.net.InetAddress;
import java.time.Clock;
import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Troca de senha")
class ChangePasswordServiceTest {

    private static final int SESSION = 10;
    private static final Password CURRENT = Password.of("Senha@Segura123");
    private static final Password NEW = Password.of("Nova@Senha4567");

    private final RateLimitPolicy rateLimitPolicy = Policies.account(new Limit(100, 100, Duration.ofSeconds(1)));
    private final LocalCredentialRepository repository = mock(LocalCredentialRepository.class);
    private final SessionService sessionService = mock(SessionService.class);
    private final AccessService accessService = mock(AccessService.class);
    private final InetAddress ip = InetAddress.getLoopbackAddress();
    private final PasswordEncoder encoder = spy(new BCryptPasswordEncoder(4));
    private final LocalCredentialService localCredentialService =
            new LocalCredentialService(repository, mock(LocalCredentialMapper.class), encoder, Clock.systemUTC());
    private final PasswordService passwordService = new PasswordService(localCredentialService, sessionService, accessService);
    private final ChangePasswordService changePasswordService =
            new ChangePasswordService(new PasswordAttemptService(rateLimitPolicy, localCredentialService), localCredentialService, passwordService);

    private final User user = Users.ana();
    private String stored;

    @AfterEach
    void close() {
        rateLimitPolicy.close();
    }

    @BeforeEach
    void storedCredential() {
        stored = encoder.encode(CURRENT.value());
        LocalCredential credential = LocalCredential.builder().id(user.getId()).user(user)
                .passwordHash(PasswordHash.of(stored)).build();
        when(repository.findByUserUuid(user.getUuid())).thenReturn(Optional.of(credential));
    }

    @Test
    @Controle
    @DisplayName("troca a senha pela procedure, encerra todas as sessões e abre uma nova")
    void changesPasswordAndRevokesOtherSessions() {
        changePasswordService.change(user.getUuid(), SESSION, CURRENT, NEW, ip);

        ArgumentCaptor<String> hash = ArgumentCaptor.forClass(String.class);
        verify(repository).change(eq(user.getId()), eq(stored), hash.capture());
        assertThat(encoder.matches(NEW.value(), hash.getValue())).isTrue();
        verify(sessionService).revokeAll(user.getUuid(), SESSION);
    }

    @Test
    @Controle
    @DisplayName("todo o cálculo de senha acontece antes de qualquer gravação")
    void hashingHappensBeforeWrites() {
        changePasswordService.change(user.getUuid(), SESSION, CURRENT, NEW, ip);

        InOrder order = inOrder(encoder, repository, sessionService, accessService);
        order.verify(encoder).matches(eq(CURRENT.value()), anyString());
        order.verify(encoder).matches(eq(NEW.value()), anyString());
        order.verify(encoder).encode(NEW.value());
        order.verify(sessionService).revokeAll(user.getUuid(), SESSION);
        order.verify(repository).change(anyInt(), anyString(), anyString());
        order.verify(accessService).rotate(any(), eq(ip));
    }

    @Test
    @Controle
    @DisplayName("senha atual errada não altera nada")
    void wrongCurrentPasswordChangesNothing() {
        assertThatThrownBy(() -> changePasswordService.change(user.getUuid(), SESSION, NEW, NEW, ip))
                .isInstanceOf(CredentialException.class)
                .extracting("problem").isEqualTo(CredentialProblem.INCORRECT_CURRENT_PASSWORD);

        verify(repository, never()).change(anyInt(), anyString(), anyString());
        verify(sessionService, never()).revokeAll(any(), any());
    }

    @Test
    @Controle
    @DisplayName("nova senha igual à atual é recusada e as sessões não são revogadas")
    void sameNewPasswordIsRejected() {
        assertThatThrownBy(() -> changePasswordService.change(user.getUuid(), SESSION, CURRENT, CURRENT, ip))
                .isInstanceOf(CredentialException.class)
                .extracting("problem").isEqualTo(CredentialProblem.SAME_PASSWORD);

        verify(repository, never()).change(anyInt(), anyString(), anyString());
        verify(sessionService, never()).revokeAll(any(), any());
    }

    @Test
    @Controle
    @DisplayName("troca concorrente (hash já alterado por outra requisição) falha")
    void concurrentChangeFails() {
        doThrow(new DataIntegrityViolationException("rn_senha_alterada"))
                .when(repository).change(anyInt(), anyString(), anyString());

        assertThatThrownBy(() -> changePasswordService.change(user.getUuid(), SESSION, CURRENT, NEW, ip))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Controle
    @DisplayName("sessão atual revogada impede a troca de senha")
    void revokedSessionChangesNothing() {
        doThrow(new SessionException(SessionProblem.REVOKED))
                .when(sessionService).revokeAll(user.getUuid(), SESSION);

        assertThatThrownBy(() -> changePasswordService.change(user.getUuid(), SESSION, CURRENT, NEW, ip))
                .isInstanceOf(SessionException.class);

        verify(repository, never()).change(anyInt(), anyString(), anyString());
    }
}
