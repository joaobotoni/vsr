package com.botoni.vsr.service;

import com.botoni.vsr.configuration.PasswordEncoderConfig;
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
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@DisplayName("Credencial local")
class LocalCredentialServiceTest {

    private static final Password CURRENT = Password.of("senha segura 123");
    private static final Password NEW = Password.of("nova senha 4567");
    private static final Password WRONG = Password.of("senha errada 123");

    private final LocalCredentialRepository repository = mock(LocalCredentialRepository.class);
    private final PasswordEncoder encoder = new PasswordEncoderConfig().passwordEncoder();
    private final LocalCredentialService localCredentialService = new LocalCredentialService(
            repository, Mappers.getMapper(LocalCredentialMapper.class), encoder, Clock.systemUTC());

    private final User user = Users.ana();
    private LocalCredential credential;

    @BeforeEach
    void storedCredential() {
        credential = LocalCredential.builder().id(user.getId()).user(user)
                .passwordHash(PasswordHash.of(encoder.encode(CURRENT.value())))
                .passwordUpdatedAt(Instant.EPOCH).build();
        when(repository.findByUserUuid(any())).thenReturn(Optional.empty());
        when(repository.findByUserUuid(Users.UUID)).thenReturn(Optional.of(credential));
        when(repository.save(any())).then(returnsFirstArg());
    }

    @Test
    @Controle
    @DisplayName("a senha vira hash Argon2id, nunca texto")
    void passwordIsHashedWithArgon2() {
        PasswordHash hash = localCredentialService.hash(CURRENT);

        assertThat(hash.value()).startsWith("$argon2id$").doesNotContain(CURRENT.value());
        assertThat(encoder.matches(CURRENT.value(), hash.value())).isTrue();
    }

    @Test
    @Controle
    @DisplayName("salvar grava a credencial com o hash recebido, sem calcular outro")
    void saveStoresGivenHash() {
        PasswordHash hash = localCredentialService.hash(CURRENT);

        localCredentialService.save(user, hash);

        ArgumentCaptor<LocalCredential> saved = ArgumentCaptor.forClass(LocalCredential.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().getPasswordHash()).isEqualTo(hash);
        assertThat(saved.getValue().getUser()).isSameAs(user);
    }

    @Test
    @Controle
    @DisplayName("senha atual correta é verificada")
    void correctPasswordIsVerified() {
        assertThat(localCredentialService.verify(Users.UUID, CURRENT)).isSameAs(credential);
    }

    @Test
    @Controle
    @DisplayName("senha atual errada é recusada")
    void wrongPasswordIsRejected() {
        assertThatThrownBy(() -> localCredentialService.verify(Users.UUID, WRONG))
                .isInstanceOf(CredentialException.class)
                .extracting("problem").isEqualTo(CredentialProblem.INCORRECT_CURRENT_PASSWORD);
    }

    @Test
    @Controle
    @DisplayName("usuário sem credencial local é recusado")
    void missingCredentialIsRejected() {
        assertThatThrownBy(() -> localCredentialService.verify(UUID.randomUUID(), CURRENT))
                .isInstanceOf(CredentialException.class)
                .extracting("problem").isEqualTo(CredentialProblem.NOT_FOUND);
    }

    @Test
    @Controle
    @DisplayName("o novo hash é calculado sem tocar no banco")
    void renewDoesNotTouchDatabase() {
        PasswordHash hash = localCredentialService.renew(credential, NEW);

        assertThat(encoder.matches(NEW.value(), hash.value())).isTrue();
        verifyNoInteractions(repository);
    }

    @Test
    @Controle
    @DisplayName("nova senha igual à atual é recusada antes de calcular o hash")
    void samePasswordIsRejected() {
        assertThatThrownBy(() -> localCredentialService.renew(credential, CURRENT))
                .isInstanceOf(CredentialException.class)
                .extracting("problem").isEqualTo(CredentialProblem.SAME_PASSWORD);
        verifyNoInteractions(repository);
    }

    @Test
    @Controle
    @DisplayName("substituir grava pela procedure só se o hash ainda for o lido")
    void replaceUsesCompareAndSet() {
        String current = credential.getPasswordHash().value();
        PasswordHash hash = localCredentialService.renew(credential, NEW);

        localCredentialService.replace(credential, hash);

        verify(repository).change(user.getId(), current, hash.value());
    }
}
