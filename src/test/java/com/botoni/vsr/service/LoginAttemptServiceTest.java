package com.botoni.vsr.service;

import com.botoni.vsr.exception.custom.RateLimitException;
import com.botoni.vsr.exception.enums.problem.RateLimitProblem;
import com.botoni.vsr.ratelimit.RateLimitPolicy;
import com.botoni.vsr.support.Policies;
import com.botoni.vsr.ratelimit.TokenBucket.Limit;
import com.botoni.vsr.support.Controle;
import com.botoni.vsr.support.Users;
import com.botoni.vsr.vo.Email;
import com.botoni.vsr.vo.Password;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Tentativas de login por conta")
class LoginAttemptServiceTest {

    private static final int CAPACITY = 3;
    private static final int CEILING = 6;
    private static final Email ANA = Email.of("ana@vsr.com");
    private static final Email BIA = Email.of("bia@vsr.com");
    private static final Password PASSWORD = Password.of("Senha@Qualquer1");
    private static final InetAddress HOME = ip(203, 0, 113, 7);
    private static final InetAddress ELSEWHERE = ip(198, 51, 100, 9);
    private static final InetAddress THIRD = ip(192, 0, 2, 44);

    private final RateLimitPolicy rateLimitPolicy = Policies.of(new Limit(CAPACITY, 1, Duration.ofHours(1)), new Limit(CEILING, 1, Duration.ofHours(1)));
    private final AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
    private final LoginAttemptService loginAttemptService =
            new LoginAttemptService(rateLimitPolicy, authenticationManager);

    @AfterEach
    void close() {
        rateLimitPolicy.close();
    }

    @Test
    @Controle
    @DisplayName("senha certa devolve a autenticação do Spring")
    void rightPasswordAuthenticates() {
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(Users.principal(), null, List.of());
        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        assertThat(loginAttemptService.verify(ANA, PASSWORD, HOME)).isSameAs(authentication);
    }

    @Test
    @Controle
    @DisplayName("senha errada é recusada com a exceção de credenciais do Spring")
    void wrongPasswordIsRejected() {
        wrong();

        assertThatThrownBy(() -> loginAttemptService.verify(ANA, PASSWORD, HOME)).isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @Controle
    @DisplayName("toda tentativa conta; esgotada a conta naquela rede, recusa com 429 sem conferir a senha")
    void exhaustedNetworkIsRejectedBeforePassword() {
        wrong();
        exhaust(ANA, HOME, CAPACITY);
        reset(authenticationManager);

        assertThatThrownBy(() -> loginAttemptService.verify(ANA, PASSWORD, HOME))
                .isInstanceOf(RateLimitException.class)
                .extracting("problem").isEqualTo(RateLimitProblem.EXCEEDED);
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    @Controle
    @DisplayName("quem ataca de uma rede não tranca a vítima, que segue entrando da própria rede")
    void attackerNetworkDoesNotLockOutVictim() {
        wrong();
        exhaust(ANA, ELSEWHERE, CAPACITY);
        assertThatThrownBy(() -> loginAttemptService.verify(ANA, PASSWORD, ELSEWHERE)).isInstanceOf(RateLimitException.class);

        assertThatThrownBy(() -> loginAttemptService.verify(ANA, PASSWORD, HOME)).isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @Controle
    @DisplayName("a rede que já estourou não gasta o teto da conta")
    void exhaustedNetworkDoesNotSpendCeiling() {
        wrong();
        exhaust(ANA, ELSEWHERE, CAPACITY);
        for (int i = 0; i < CEILING * 3; i++) {
            assertThatThrownBy(() -> loginAttemptService.verify(ANA, PASSWORD, ELSEWHERE)).isInstanceOf(RateLimitException.class);
        }

        exhaust(ANA, HOME, CAPACITY);
    }

    @Test
    @Controle
    @DisplayName("força bruta espalhada por várias redes esbarra no teto da conta")
    void distributedAttackHitsCeiling() {
        wrong();
        exhaust(ANA, HOME, CAPACITY);
        exhaust(ANA, ELSEWHERE, CAPACITY);

        assertThatThrownBy(() -> loginAttemptService.verify(ANA, PASSWORD, THIRD)).isInstanceOf(RateLimitException.class);
    }

    @Test
    @Controle
    @DisplayName("contas diferentes têm limites independentes")
    void accountsAreIndependent() {
        wrong();
        exhaust(ANA, HOME, CAPACITY);

        assertThatThrownBy(() -> loginAttemptService.verify(BIA, PASSWORD, HOME)).isInstanceOf(BadCredentialsException.class);
    }

    private void wrong() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("senha errada"));
    }

    private void exhaust(Email email, InetAddress ip, int times) {
        for (int i = 0; i < times; i++) {
            assertThatThrownBy(() -> loginAttemptService.verify(email, PASSWORD, ip)).isInstanceOf(BadCredentialsException.class);
        }
    }

    private static InetAddress ip(int a, int b, int c, int d) {
        try {
            return InetAddress.getByAddress(new byte[]{(byte) a, (byte) b, (byte) c, (byte) d});
        } catch (UnknownHostException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
