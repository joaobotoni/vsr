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

import java.net.InetAddress;
import java.net.UnknownHostException;
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
    private static final int CEILING = 6;
    private static final Email ANA = Email.of("ana@vsr.com");
    private static final Password PASSWORD = Password.of("Senha@Qualquer1");
    private static final UUID PHONE = UUID.randomUUID();
    private static final UUID STRANGER = UUID.randomUUID();
    private static final InetAddress HOME = ip(203, 0, 113, 7);
    private static final InetAddress ELSEWHERE = ip(198, 51, 100, 9);

    private final AttemptLimiter attemptLimiter = AttemptLimiter.celled(
            new Limit(CAPACITY, 1, Duration.ofHours(1)), new Limit(CEILING, 1, Duration.ofHours(1)));
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

        assertThatThrownBy(() -> loginAttemptService.verify(ANA, PASSWORD, STRANGER, HOME))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @Controle
    @DisplayName("depois do limite de falhas, a conta é recusada sem nem conferir a senha")
    void failuresExhaustAccount() {
        fail(STRANGER, CAPACITY);

        assertThatThrownBy(() -> loginAttemptService.verify(ANA, PASSWORD, STRANGER, HOME))
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
            assertThat(loginAttemptService.verify(ANA, PASSWORD, STRANGER, HOME)).isSameAs(authenticated);
        }
    }

    @Test
    @Controle
    @DisplayName("falha de infraestrutura passa adiante e devolve a tentativa")
    void infrastructureFailureRefundsAttempt() {
        when(authenticationManager.authenticate(any())).thenThrow(new InternalAuthenticationServiceException("banco indisponível"));

        for (int i = 0; i < CAPACITY * 3; i++) {
            assertThatThrownBy(() -> loginAttemptService.verify(ANA, PASSWORD, STRANGER, HOME))
                    .isInstanceOf(InternalAuthenticationServiceException.class);
        }
    }

    @Test
    @Controle
    @DisplayName("falhas vindas de dispositivos desconhecidos não trancam um dispositivo já usado pela conta")
    void knownDeviceHasOwnLimit() {
        when(deviceService.known(ANA, PHONE)).thenReturn(true);
        fail(STRANGER, CAPACITY);

        assertThatThrownBy(() -> loginAttemptService.verify(ANA, PASSWORD, PHONE, HOME)).isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @Controle
    @DisplayName("trocar o identificador por outro desconhecido não dá tentativas novas")
    void rotatingUnknownDevicesSharesLimit() {
        fail(UUID.randomUUID(), CAPACITY);

        assertThatThrownBy(() -> loginAttemptService.verify(ANA, PASSWORD, UUID.randomUUID(), HOME))
                .isInstanceOf(RateLimitException.class);
    }

    @Test
    @Controle
    @DisplayName("e-mail com maiúsculas ou espaços conta na mesma conta")
    void emailVariationsShareLimit() {
        fail(STRANGER, CAPACITY);

        assertThatThrownBy(() -> loginAttemptService.verify(Email.of("  ANA@vsr.com "), PASSWORD, STRANGER, HOME))
                .isInstanceOf(RateLimitException.class);
    }

    @Test
    @Controle
    @DisplayName("outra conta não é afetada")
    void otherAccountIsNotAffected() {
        fail(STRANGER, CAPACITY);

        assertThatThrownBy(() -> loginAttemptService.verify(Email.of("bia@vsr.com"), PASSWORD, STRANGER, HOME))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @Controle
    @DisplayName("falhas vindas de uma rede não trancam a conta para quem tenta de outra rede")
    void otherNetworkIsNotLockedOut() {
        fail(STRANGER, CAPACITY);

        assertThatThrownBy(() -> loginAttemptService.verify(ANA, PASSWORD, STRANGER, ELSEWHERE))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @Controle
    @DisplayName("o teto da conta soma as falhas de todas as redes: espalhar o ataque não dá tentativas ilimitadas")
    void ceilingSumsAllNetworks() {
        rejectAll();
        for (int i = 0; i < CEILING; i++) {
            failFrom(ip(203, 0, 113, 10 + i), 1);
        }

        assertThatThrownBy(() -> loginAttemptService.verify(ANA, PASSWORD, STRANGER, ip(203, 0, 113, 99)))
                .isInstanceOf(RateLimitException.class);
        verify(authenticationManager, times(CEILING)).authenticate(any());
    }

    @Test
    @Controle
    @DisplayName("rede que já estourou o próprio limite não gasta o teto da conta")
    void blockedNetworkDoesNotDrainCeiling() {
        fail(STRANGER, CAPACITY);
        for (int i = 0; i < 20; i++) {
            assertThatThrownBy(() -> loginAttemptService.verify(ANA, PASSWORD, STRANGER, HOME))
                    .isInstanceOf(RateLimitException.class);
        }

        failFrom(ELSEWHERE, CEILING - CAPACITY);
    }

    @Test
    @Controle
    @DisplayName("com o teto da conta esgotado, o dispositivo que a vítima já usa continua entrando")
    void knownDeviceIgnoresCeiling() {
        when(deviceService.known(ANA, PHONE)).thenReturn(true);
        rejectAll();
        for (int i = 0; i < CEILING; i++) {
            failFrom(ip(203, 0, 113, 10 + i), 1);
        }

        assertThatThrownBy(() -> loginAttemptService.verify(ANA, PASSWORD, PHONE, ip(203, 0, 113, 99)))
                .isInstanceOf(BadCredentialsException.class);
    }

    private void fail(UUID device, int times) {
        rejectAll();
        for (int i = 0; i < times; i++) {
            assertThatThrownBy(() -> loginAttemptService.verify(ANA, PASSWORD, device, HOME)).isInstanceOf(BadCredentialsException.class);
        }
    }

    private void failFrom(InetAddress ip, int times) {
        for (int i = 0; i < times; i++) {
            assertThatThrownBy(() -> loginAttemptService.verify(ANA, PASSWORD, STRANGER, ip)).isInstanceOf(BadCredentialsException.class);
        }
    }

    private static InetAddress ip(int a, int b, int c, int d) {
        try {
            return InetAddress.getByAddress(new byte[]{(byte) a, (byte) b, (byte) c, (byte) d});
        } catch (UnknownHostException exception) {
            throw new IllegalArgumentException(exception);
        }
    }

    private void rejectAll() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));
    }
}
