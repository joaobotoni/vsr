package com.botoni.vsr.service;

import com.botoni.vsr.exception.custom.RateLimitException;
import com.botoni.vsr.exception.enums.problem.RateLimitProblem;
import com.botoni.vsr.ratelimit.Limit;
import com.botoni.vsr.ratelimit.RateLimit;
import com.botoni.vsr.support.Controle;
import com.botoni.vsr.vo.Email;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Tentativas de login por conta")
class LoginAttemptServiceTest {

    private static final int CAPACITY = 3;
    private static final Email ANA = Email.of("ana@vsr.com");

    private final RateLimit accountRateLimit = RateLimit.any(new Limit(CAPACITY, 1, Duration.ofHours(1)));
    private final LoginAttemptService loginAttemptService = new LoginAttemptService(accountRateLimit);

    @AfterEach
    void close() {
        accountRateLimit.close();
    }

    @Test
    @Controle
    @DisplayName("depois do limite de falhas, a conta é recusada de qualquer IP")
    void failuresExhaustAccount() {
        fail(ANA, CAPACITY);

        assertThatThrownBy(() -> loginAttemptService.check(ANA))
                .isInstanceOf(RateLimitException.class)
                .extracting("problem").isEqualTo(RateLimitProblem.EXCEEDED);
    }

    @Test
    @Controle
    @DisplayName("conferir o limite não gasta tentativa: logins certos nunca trancam a conta")
    void checkDoesNotConsume() {
        for (int i = 0; i < CAPACITY * 10; i++) {
            loginAttemptService.check(ANA);
        }

        assertThatCode(() -> loginAttemptService.check(ANA)).doesNotThrowAnyException();
    }

    @Test
    @Controle
    @DisplayName("e-mail com maiúsculas ou espaços conta na mesma conta")
    void emailVariationsShareLimit() {
        fail(ANA, CAPACITY);

        assertThatThrownBy(() -> loginAttemptService.check(Email.of("  ANA@vsr.com ")))
                .isInstanceOf(RateLimitException.class);
    }

    @Test
    @Controle
    @DisplayName("outra conta não é afetada")
    void otherAccountIsNotAffected() {
        fail(ANA, CAPACITY);

        assertThatCode(() -> loginAttemptService.check(Email.of("bia@vsr.com"))).doesNotThrowAnyException();
    }

    private void fail(Email email, int times) {
        for (int i = 0; i < times; i++) {
            loginAttemptService.fail(email);
        }
    }
}
