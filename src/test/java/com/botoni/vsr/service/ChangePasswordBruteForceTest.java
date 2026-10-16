package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.LocalCredential;
import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.database.repository.LocalCredentialRepository;
import com.botoni.vsr.exception.custom.CredentialException;
import com.botoni.vsr.exception.custom.RateLimitException;
import com.botoni.vsr.exception.enums.problem.CredentialProblem;
import com.botoni.vsr.mapper.LocalCredentialMapper;
import com.botoni.vsr.ratelimit.Limit;
import com.botoni.vsr.ratelimit.AttemptLimiter;
import com.botoni.vsr.support.Controle;
import com.botoni.vsr.support.Users;
import com.botoni.vsr.vo.Password;
import com.botoni.vsr.vo.PasswordHash;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Força bruta pela troca de senha")
class ChangePasswordBruteForceTest {

    private static final int SESSION = 10;
    private static final int CAPACITY = 3;
    private static final Password CURRENT = Password.of("Senha@Segura123");
    private static final Password NEW = Password.of("Nova@Senha4567");

    private final AttemptLimiter attemptLimiter = AttemptLimiter.from(new Limit(CAPACITY, 1, Duration.ofHours(1)));
    private final LocalCredentialRepository repository = mock(LocalCredentialRepository.class);
    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final LocalCredentialService localCredentialService =
            new LocalCredentialService(repository, mock(LocalCredentialMapper.class), encoder, Clock.systemUTC());
    private final ChangePasswordService changePasswordService = new ChangePasswordService(
            new PasswordAttemptService(attemptLimiter, localCredentialService), localCredentialService,
            new PasswordService(localCredentialService, mock(SessionService.class), mock(AccessService.class)));

    private final User user = Users.ana();

    @BeforeEach
    void storedCredential() {
        LocalCredential credential = LocalCredential.builder().id(user.getId()).user(user)
                .passwordHash(PasswordHash.of(encoder.encode(CURRENT.value()))).build();
        when(repository.findByUserUuid(user.getUuid())).thenReturn(Optional.of(credential));
    }

    @AfterEach
    void close() {
        attemptLimiter.close();
    }

    @Test
    @Controle
    @DisplayName("depois do limite de senhas atuais erradas, a conta é recusada mesmo com a senha certa")
    void wrongGuessesExhaustAccount() {
        guess(CAPACITY);

        assertThatThrownBy(() -> change(CURRENT)).isInstanceOf(RateLimitException.class);
    }

    @Test
    @Controle
    @DisplayName("senha atual certa devolve a tentativa: trocas legítimas nunca trancam a conta")
    void correctPasswordRefundsAttempt() {
        for (int i = 0; i < CAPACITY * 3; i++) {
            assertThatCode(() -> change(CURRENT)).doesNotThrowAnyException();
        }
    }

    @Test
    @Controle
    @DisplayName("falha de infraestrutura passa adiante e devolve a tentativa")
    void infrastructureFailureRefundsAttempt() {
        when(repository.findByUserUuid(user.getUuid())).thenThrow(new DataAccessResourceFailureException("banco indisponível"));

        for (int i = 0; i < CAPACITY * 3; i++) {
            assertThatThrownBy(() -> change(CURRENT)).isInstanceOf(DataAccessResourceFailureException.class);
        }
    }

    private void guess(int times) {
        for (int i = 0; i < times; i++) {
            Password guess = Password.of("Chute@Numero" + i);
            assertThatThrownBy(() -> change(guess))
                    .isInstanceOf(CredentialException.class)
                    .extracting("problem").isEqualTo(CredentialProblem.INCORRECT_CURRENT_PASSWORD);
        }
    }

    private void change(Password current) {
        changePasswordService.change(user.getUuid(), SESSION, current, NEW, null);
    }
}
