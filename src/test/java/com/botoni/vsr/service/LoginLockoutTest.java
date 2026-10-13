package com.botoni.vsr.service;

import com.botoni.vsr.database.enums.DevicePlatform;
import com.botoni.vsr.dto.request.DeviceRequest;
import com.botoni.vsr.dto.request.LoginRequest;
import com.botoni.vsr.exception.custom.RateLimitException;
import com.botoni.vsr.ratelimit.Limit;
import com.botoni.vsr.ratelimit.RateLimit;
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

import java.net.InetAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Bloqueio de conta no login")
class LoginLockoutTest {

    private static final int CAPACITY = 3;
    private static final int PARALLEL = 20;
    private static final String RIGHT = "senha segura 123";
    private static final String WRONG = "senha errada 999";
    private static final UUID PHONE = UUID.randomUUID();

    private final RateLimit accountRateLimit = RateLimit.any(new Limit(CAPACITY, 1, Duration.ofHours(1)));
    private final AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
    private final DeviceService deviceService = mock(DeviceService.class);
    private final LoginAttemptService loginAttemptService =
            new LoginAttemptService(new AttemptService(accountRateLimit), deviceService, authenticationManager);
    private final UserService userService = mock(UserService.class);
    private final LoginService loginService = new LoginService(loginAttemptService, userService, mock(AccessService.class));

    @AfterEach
    void close() {
        accountRateLimit.close();
    }

    @Test
    @Controle
    @DisplayName("requisições paralelas não furam o limite: só a capacidade da conta chega a conferir a senha")
    void parallelAttemptsRespectAccountLimit() throws Exception {
        AtomicInteger guesses = new AtomicInteger();
        when(authenticationManager.authenticate(any())).thenAnswer(invocation -> {
            guesses.incrementAndGet();
            throw new BadCredentialsException("senha errada");
        });
        CountDownLatch start = new CountDownLatch(1);

        ExecutorService pool = Executors.newFixedThreadPool(PARALLEL);
        try {
            List<Future<?>> attempts = new ArrayList<>();
            for (int i = 0; i < PARALLEL; i++) {
                attempts.add(pool.submit(() -> {
                    start.await();
                    attempt(WRONG, UUID.randomUUID());
                    return null;
                }));
            }
            start.countDown();
            for (Future<?> attempt : attempts) {
                attempt.get(10, TimeUnit.SECONDS);
            }
        } finally {
            pool.shutdownNow();
        }

        assertThat(guesses.get()).isEqualTo(CAPACITY);
    }

    @Test
    @Controle
    @DisplayName("quem conhece só o e-mail não tranca a vítima, que entra pelo dispositivo que já usa")
    void attackerCannotLockOutVictimDevice() {
        when(deviceService.isKnown(Email.of(Users.EMAIL), PHONE)).thenReturn(true);
        when(authenticationManager.authenticate(any())).thenAnswer(invocation -> {
            UsernamePasswordAuthenticationToken token = invocation.getArgument(0);
            if (!RIGHT.equals(token.getCredentials())) {
                throw new BadCredentialsException("senha errada");
            }
            return UsernamePasswordAuthenticationToken.authenticated(Users.principal(), null, List.of());
        });
        when(userService.find(any())).thenReturn(Users.ana());

        for (int i = 0; i < CAPACITY * 3; i++) {
            attempt(WRONG, UUID.randomUUID());
        }

        assertThatThrownBy(() -> loginService.login(request(RIGHT, UUID.randomUUID()), InetAddress.getLoopbackAddress()))
                .isInstanceOf(RateLimitException.class);
        assertThatCode(() -> loginService.login(request(RIGHT, PHONE), InetAddress.getLoopbackAddress()))
                .doesNotThrowAnyException();
    }

    private void attempt(String password, UUID device) {
        try {
            loginService.login(request(password, device), InetAddress.getLoopbackAddress());
        } catch (BadCredentialsException | RateLimitException ignored) {
            // falha esperada; o que importa é quantas tentativas chegaram à senha
        }
    }

    private static LoginRequest request(String password, UUID device) {
        return new LoginRequest(Email.of(Users.EMAIL), Password.of(password),
                new DeviceRequest(device, DevicePlatform.ANDROID, "Samsung", "S23", "14"));
    }
}
