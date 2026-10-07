package com.botoni.vsr.configuration;

import com.botoni.vsr.database.entity.LocalCredential;
import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.database.repository.LocalCredentialRepository;
import com.botoni.vsr.support.Brecha;
import com.botoni.vsr.support.Controle;
import com.botoni.vsr.support.Users;
import com.botoni.vsr.vo.Email;
import com.botoni.vsr.vo.PasswordHash;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Login com e-mail e senha")
class AuthenticationConfigTest {

    private static final String PASSWORD = "senha segura 123";
    private static final String WRONG = "senha errada 123";

    private final LocalCredentialRepository repository = mock(LocalCredentialRepository.class);
    private final PasswordEncoder encoder = new PasswordEncoderConfig().passwordEncoder();
    private final AuthenticationConfig config = new AuthenticationConfig();
    private final AuthenticationManager manager =
            new ProviderManager(config.authenticationProvider(config.userDetailsService(repository), encoder));

    private String hash;

    @BeforeEach
    void registeredUser() {
        hash = encoder.encode(PASSWORD);
        User user = Users.ana();
        LocalCredential credential = LocalCredential.builder().id(user.getId()).user(user).passwordHash(PasswordHash.of(hash)).build();
        when(repository.findWithUserByEmail(any())).thenReturn(Optional.empty());
        when(repository.findWithUserByEmail(Email.of(Users.EMAIL))).thenReturn(Optional.of(credential));
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

    private Authentication authenticate(String email, String password) {
        return manager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(email, password));
    }
}
