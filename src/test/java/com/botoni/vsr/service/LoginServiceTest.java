package com.botoni.vsr.service;

import com.botoni.vsr.database.enums.DevicePlatform;
import com.botoni.vsr.dto.request.DeviceRequest;
import com.botoni.vsr.dto.request.LoginRequest;
import com.botoni.vsr.exception.custom.RateLimitException;
import com.botoni.vsr.exception.enums.problem.RateLimitProblem;
import com.botoni.vsr.support.Controle;
import com.botoni.vsr.support.Users;
import com.botoni.vsr.vo.Email;
import com.botoni.vsr.vo.Password;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.net.InetAddress;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@DisplayName("Login")
class LoginServiceTest {

    private final LoginAttemptService loginAttemptService = mock(LoginAttemptService.class);
    private final UserService userService = mock(UserService.class);
    private final AccessService accessService = mock(AccessService.class);
    private final LoginService loginService = new LoginService(loginAttemptService, userService, accessService);

    private final LoginRequest request = new LoginRequest(Email.of("ana@vsr.com"), Password.of("senha segura 123"),
            new DeviceRequest(UUID.randomUUID(), DevicePlatform.ANDROID, "Samsung", "S23", "14"));

    @Test
    @Controle
    @DisplayName("a tentativa é conferida com o e-mail, a senha e o identificador do dispositivo")
    void attemptUsesDevice() {
        authenticated();

        login();

        verify(loginAttemptService).verify(request.email(), request.password(), request.device().identifier());
    }

    @Test
    @Controle
    @DisplayName("com o limite estourado não busca o usuário nem abre sessão")
    void exceededLimitStopsLogin() {
        when(attempt()).thenThrow(new RateLimitException(RateLimitProblem.EXCEEDED, 60L));

        assertThatThrownBy(this::login).isInstanceOf(RateLimitException.class);

        verifyNoInteractions(userService, accessService);
    }

    @Test
    @Controle
    @DisplayName("senha errada não busca o usuário nem abre sessão")
    void wrongPasswordStopsLogin() {
        when(attempt()).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(this::login).isInstanceOf(BadCredentialsException.class);

        verifyNoInteractions(userService, accessService);
    }

    @Test
    @Controle
    @DisplayName("login certo carrega o usuário junto com a pessoa, que a resposta usa fora da transação da busca")
    void userIsLoadedWithPerson() {
        authenticated();

        login();

        verify(userService).find(Users.UUID);
    }

    private void authenticated() {
        when(attempt()).thenReturn(UsernamePasswordAuthenticationToken.authenticated(Users.principal(), null, List.of()));
    }

    private Authentication attempt() {
        return loginAttemptService.verify(request.email(), request.password(), request.device().identifier());
    }

    private void login() {
        loginService.login(request, InetAddress.getLoopbackAddress());
    }
}
