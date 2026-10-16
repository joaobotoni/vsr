package com.botoni.vsr.configuration;

import com.botoni.vsr.database.entity.LocalCredential;
import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.database.repository.LocalCredentialRepository;
import com.botoni.vsr.mapper.LocalCredentialMapper;
import com.botoni.vsr.service.LocalCredentialService;
import com.botoni.vsr.support.Brecha;
import com.botoni.vsr.support.Controle;
import com.botoni.vsr.support.Users;
import com.botoni.vsr.vo.Email;
import com.botoni.vsr.vo.PasswordHash;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Login com e-mail e senha")
class AuthenticationConfigTest {

    private static final String PASSWORD = "Senha@Segura123";
    private static final String WRONG = "Senha@Errada123";

    private final LocalCredentialRepository repository = mock(LocalCredentialRepository.class);
    private final PasswordEncoder encoder = new PasswordEncoderConfig().passwordEncoder();
    private final AuthenticationConfig config = new AuthenticationConfig();
    private final LocalCredentialService localCredentialService = new LocalCredentialService(
            repository, Mappers.getMapper(LocalCredentialMapper.class), encoder, Clock.systemUTC());
    private final AuthenticationManager manager = new ProviderManager(config.authenticationProvider(
            config.userDetailsService(repository), config.userDetailsPasswordService(localCredentialService), encoder));

    private String hash;

    @BeforeEach
    void registeredUser() {
        store(encoder.encode(PASSWORD));
    }

    @Test
    @Controle
    @DisplayName("senha correta autentica")
    void correctPasswordAuthenticates() {
        assertThat(authenticate(Users.EMAIL, PASSWORD).isAuthenticated()).isTrue();
    }

    @Test
    @Controle
    @DisplayName("usuário inexistente e senha errada geram o mesmo erro")
    void unknownUserLooksLikeWrongPassword() {
        assertThatThrownBy(() -> authenticate("ninguem@vsr.com", PASSWORD)).isExactlyInstanceOf(BadCredentialsException.class);
        assertThatThrownBy(() -> authenticate(Users.EMAIL, WRONG)).isExactlyInstanceOf(BadCredentialsException.class);
    }

    @Test
    @Controle
    @DisplayName("senha não fica guardada na autenticação devolvida")
    void credentialsAreErasedFromResult() {
        assertThat(authenticate(Users.EMAIL, PASSWORD).getCredentials()).isNull();
    }

    @Test
    @Brecha
    @DisplayName("a conta não é bloqueada de forma persistente após muitas senhas erradas")
    void noAccountLockout() {
        for (int i = 0; i < 25; i++) {
            assertThatThrownBy(() -> authenticate(Users.EMAIL, WRONG)).isInstanceOf(BadCredentialsException.class);
        }
        assertThat(authenticate(Users.EMAIL, PASSWORD).isAuthenticated()).isTrue();
    }

    @Test
    @Controle
    @DisplayName("senha é guardada com Argon2id (19 MiB, 2 iterações) e o hash cabe no VO")
    void passwordUsesArgon2id() {
        assertThat(hash).startsWith("$argon2id$v=19$m=19456,t=2,p=1$");
        assertThat(PasswordHash.of(hash).value()).isEqualTo(hash);
    }

    @Test
    @Controle
    @DisplayName("hash com parâmetros antigos do Argon2 é recalculado e gravado após o login correto")
    void weakerHashIsUpgradedOnLogin() {
        String weaker = new Argon2PasswordEncoder(16, 32, 1, 4096, 1).encode(PASSWORD);
        store(weaker);

        assertThat(authenticate(Users.EMAIL, PASSWORD).isAuthenticated()).isTrue();

        ArgumentCaptor<String> upgraded = ArgumentCaptor.forClass(String.class);
        verify(repository).change(eq(Users.ana().getId()), eq(weaker), upgraded.capture());
        assertThat(upgraded.getValue()).startsWith("$argon2id$v=19$m=19456,t=2,p=1$");
        assertThat(encoder.matches(PASSWORD, upgraded.getValue())).isTrue();
    }

    @Test
    @Controle
    @DisplayName("hash com os parâmetros atuais não é regravado")
    void currentHashIsNotRewritten() {
        authenticate(Users.EMAIL, PASSWORD);

        verify(repository, never()).change(any(), any(), any());
    }

    @Test
    @Controle
    @DisplayName("se outra troca de senha gravar primeiro, o login segue e o hash dela é mantido")
    void concurrentChangeDoesNotBreakLogin() {
        store(new Argon2PasswordEncoder(16, 32, 1, 4096, 1).encode(PASSWORD));
        doThrow(new DataIntegrityViolationException("rn_senha_alterada")).when(repository).change(any(), any(), any());

        assertThat(authenticate(Users.EMAIL, PASSWORD).isAuthenticated()).isTrue();
    }

    private void store(String stored) {
        hash = stored;
        User user = Users.ana();
        LocalCredential credential = LocalCredential.builder().id(user.getId()).user(user).passwordHash(PasswordHash.of(hash)).build();
        when(repository.findWithUserByEmail(any())).thenReturn(Optional.empty());
        when(repository.findWithUserByEmail(Email.of(Users.EMAIL))).thenReturn(Optional.of(credential));
        when(repository.findByUserUuid(Users.UUID)).thenReturn(Optional.of(credential));
    }

    private Authentication authenticate(String email, String password) {
        return manager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(email, password));
    }
}
