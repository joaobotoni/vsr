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
import org.mockito.InOrder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.net.InetAddress;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Login")
class LoginServiceTest {

    private final LoginAttemptService loginAttemptService = mock(LoginAttemptService.class);
    private final AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
    private final UserService userService = mock(UserService.class);
    private final LoginService loginService = new LoginService(
            loginAttemptService, userService, mock(AccessService.class), authenticationManager);

    private final LoginRequest request = new LoginRequest(Email.of("ana@vsr.com"), Password.of("senha segura 123"),
            new DeviceRequest(UUID.randomUUID(), DevicePlatform.ANDROID, "Samsung", "S23", "14"));

    @Test
    @Controle
    @DisplayName("o limite da conta é conferido antes da senha")
    void limitIsCheckedBeforeAuthentication() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("senha errada"));

        assertThatThrownBy(this::login).isInstanceOf(BadCredentialsException.class);

        InOrder order = inOrder(loginAttemptService, authenticationManager);
        order.verify(loginAttemptService).check(request.email());
        order.verify(authenticationManager).authenticate(any());
    }

    @Test
    @Controle
    @DisplayName("senha errada conta uma falha para a conta")
    void wrongPasswordCountsFailure() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("senha errada"));

        assertThatThrownBy(this::login).isInstanceOf(BadCredentialsException.class);

        verify(loginAttemptService).fail(request.email());
    }

    @Test
    @Controle
    @DisplayName("login certo não conta falha")
    void correctPasswordDoesNotCountFailure() {
        when(authenticationManager.authenticate(any()))
                .thenReturn(UsernamePasswordAuthenticationToken.authenticated(Users.principal(), null, null));

        login();

        verify(loginAttemptService, never()).fail(any());
    }

    @Test
    @Controle
    @DisplayName("login certo carrega o usuário junto com a pessoa, que a resposta usa fora da transação da busca")
    void userIsLoadedWithPerson() {
        when(authenticationManager.authenticate(any()))
                .thenReturn(UsernamePasswordAuthenticationToken.authenticated(Users.principal(), null, null));

        login();

        verify(userService).find(Users.UUID);
    }

    @Test
    @Controle
    @DisplayName("com o limite da conta estourado a senha nem chega a ser conferida")
    void exceededAccountSkipsAuthentication() {
        doThrow(new RateLimitException(RateLimitProblem.EXCEEDED, 60L)).when(loginAttemptService).check(request.email());

        assertThatThrownBy(this::login).isInstanceOf(RateLimitException.class);

        verify(authenticationManager, never()).authenticate(any());
    }

    private void login() {
        loginService.login(request, InetAddress.getLoopbackAddress());
    }
}
