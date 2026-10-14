package com.botoni.vsr.service;

import com.botoni.vsr.exception.custom.RateLimitException;
import com.botoni.vsr.exception.enums.problem.RateLimitProblem;
import com.botoni.vsr.ratelimit.Limit;
import com.botoni.vsr.ratelimit.AttemptLimiter;
import com.botoni.vsr.support.Controle;
import com.botoni.vsr.support.Users;
import com.botoni.vsr.vo.Email;
import com.botoni.vsr.vo.Password;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Tentativas de login por conta")
class LoginAttemptServiceTest {

    private static final int CAPACITY = 3;
    private static final Email ANA = Email.of("ana@vsr.com");
    private static final Password PASSWORD = Password.of("senha qualquer 1");
    private static final UUID PHONE = UUID.randomUUID();
    private static final UUID STRANGER = UUID.randomUUID();

    private final AttemptLimiter attemptLimiter = AttemptLimiter.from(new Limit(CAPACITY, 1, Duration.ofHours(1)));
    private final AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
    private final DeviceService deviceService = mock(DeviceService.class);
    private final LoginAttemptService loginAttemptService =
            new LoginAttemptService(attemptLimiter, deviceService, authenticationManager);

    @AfterEach
    void close() {
        attemptLimiter.close();
    }

    @Test
    @Controle
    @DisplayName("senha errada é recusada com a exceção de credenciais do Spring")
    void wrongPasswordIsRejected() {
        rejectAll();

        assertThatThrownBy(() -> loginAttemptService.verify(ANA, PASSWORD, STRANGER))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @Controle
    @DisplayName("depois do limite de falhas, a conta é recusada sem nem conferir a senha")
    void failuresExhaustAccount() {
        fail(STRANGER, CAPACITY);

        assertThatThrownBy(() -> loginAttemptService.verify(ANA, PASSWORD, STRANGER))
                .isInstanceOf(RateLimitException.class)
                .extracting("problem").isEqualTo(RateLimitProblem.EXCEEDED);
        verify(authenticationManager, times(CAPACITY)).authenticate(any());
    }

    @Test
    @Controle
    @DisplayName("senha certa devolve a tentativa: logins certos nunca trancam a conta")
    void correctPasswordRefundsAttempt() {
        Authentication authenticated = UsernamePasswordAuthenticationToken.authenticated(Users.principal(), null, List.of());
        when(authenticationManager.authenticate(any())).thenReturn(authenticated);

        for (int i = 0; i < CAPACITY * 3; i++) {
            assertThat(loginAttemptService.verify(ANA, PASSWORD, STRANGER)).isSameAs(authenticated);
        }
    }

    @Test
    @Controle
    @DisplayName("falha de infraestrutura passa adiante e devolve a tentativa")
    void infrastructureFailureRefundsAttempt() {
        when(authenticationManager.authenticate(any())).thenThrow(new InternalAuthenticationServiceException("banco indisponível"));

        for (int i = 0; i < CAPACITY * 3; i++) {
            assertThatThrownBy(() -> loginAttemptService.verify(ANA, PASSWORD, STRANGER))
                    .isInstanceOf(InternalAuthenticationServiceException.class);
        }
    }

    @Test
    @Controle
    @DisplayName("falhas vindas de dispositivos desconhecidos não trancam um dispositivo já usado pela conta")
    void knownDeviceHasOwnLimit() {
        when(deviceService.known(ANA, PHONE)).thenReturn(true);
        fail(STRANGER, CAPACITY);

        assertThatThrownBy(() -> loginAttemptService.verify(ANA, PASSWORD, PHONE)).isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @Controle
    @DisplayName("trocar o identificador por outro desconhecido não dá tentativas novas")
    void rotatingUnknownDevicesSharesLimit() {
        fail(UUID.randomUUID(), CAPACITY);

        assertThatThrownBy(() -> loginAttemptService.verify(ANA, PASSWORD, UUID.randomUUID()))
                .isInstanceOf(RateLimitException.class);
    }

    @Test
    @Controle
    @DisplayName("e-mail com maiúsculas ou espaços conta na mesma conta")
    void emailVariationsShareLimit() {
        fail(STRANGER, CAPACITY);

        assertThatThrownBy(() -> loginAttemptService.verify(Email.of("  ANA@vsr.com "), PASSWORD, STRANGER))
                .isInstanceOf(RateLimitException.class);
    }

    @Test
    @Controle
    @DisplayName("outra conta não é afetada")
    void otherAccountIsNotAffected() {
        fail(STRANGER, CAPACITY);

        assertThatThrownBy(() -> loginAttemptService.verify(Email.of("bia@vsr.com"), PASSWORD, STRANGER))
                .isInstanceOf(BadCredentialsException.class);
    }

    private void fail(UUID device, int times) {
        rejectAll();
        for (int i = 0; i < times; i++) {
            assertThatThrownBy(() -> loginAttemptService.verify(ANA, PASSWORD, device)).isInstanceOf(BadCredentialsException.class);
        }
    }

    private void rejectAll() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));
    }
}
