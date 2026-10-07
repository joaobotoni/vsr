package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.LocalCredential;
import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.database.repository.LocalCredentialRepository;
import com.botoni.vsr.exception.custom.CredentialException;
import com.botoni.vsr.exception.enums.problem.CredentialProblem;
import com.botoni.vsr.mapper.LocalCredentialMapper;
import com.botoni.vsr.support.Controle;
import com.botoni.vsr.support.Users;
import com.botoni.vsr.vo.Password;
import com.botoni.vsr.vo.PasswordHash;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Troca de senha")
class ChangePasswordServiceTest {

    private static final int SESSION = 10;
    private static final Password CURRENT = Password.of("senha segura 123");
    private static final Password NEW = Password.of("nova senha 4567");

    private final LocalCredentialRepository repository = mock(LocalCredentialRepository.class);
    private final SessionService sessionService = mock(SessionService.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final ChangePasswordService changePasswordService = new ChangePasswordService(
            new LocalCredentialService(repository, mock(LocalCredentialMapper.class), encoder), sessionService);

    private final User user = Users.ana();
    private LocalCredential credential;

    @BeforeEach
    void storedCredential() {
        credential = LocalCredential.builder().id(user.getId()).user(user)
                .passwordHash(PasswordHash.of(encoder.encode(CURRENT.value()))).build();
        when(repository.findById(user.getId())).thenReturn(Optional.of(credential));
    }

    @Test
    @Controle
    @DisplayName("troca a senha e revoga as outras sessões, mantendo a atual")
    void changesPasswordAndRevokesOtherSessions() {
        changePasswordService.change(user, SESSION, CURRENT, NEW);

        assertThat(storedPasswordIs(NEW)).isTrue();
        verify(sessionService).revokeOthers(user, SESSION);
    }

    @Test
    @Controle
    @DisplayName("senha atual errada não altera nada")
    void wrongCurrentPasswordChangesNothing() {
        assertThatThrownBy(() -> changePasswordService.change(user, SESSION, NEW, NEW))
                .isInstanceOf(CredentialException.class)
                .extracting("problem").isEqualTo(CredentialProblem.INCORRECT_CURRENT_PASSWORD);

        assertThat(storedPasswordIs(CURRENT)).isTrue();
        verify(sessionService, never()).revokeOthers(any(), any());
    }

    @Test
    @Controle
    @DisplayName("nova senha igual à atual é recusada e as sessões não são revogadas")
    void sameNewPasswordIsRejected() {
        assertThatThrownBy(() -> changePasswordService.change(user, SESSION, CURRENT, CURRENT))
                .isInstanceOf(CredentialException.class)
                .extracting("problem").isEqualTo(CredentialProblem.SAME_PASSWORD);
        verify(sessionService, never()).revokeOthers(any(), any());
    }

    private boolean storedPasswordIs(Password password) {
        return encoder.matches(password.value(), credential.getPasswordHash().value());
    }
}
