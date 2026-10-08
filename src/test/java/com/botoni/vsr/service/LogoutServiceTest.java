package com.botoni.vsr.service;

import com.botoni.vsr.support.Controle;
import com.botoni.vsr.support.Users;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@DisplayName("Logout")
class LogoutServiceTest {

    private static final int SESSION = 10;

    private final SessionService sessionService = mock(SessionService.class);
    private final LogoutService logoutService = new LogoutService(sessionService);

    @Test
    @Controle
    @DisplayName("logout revoga só a sessão do token, do próprio usuário")
    void logoutRevokesCurrentSession() {
        logoutService.logout(Users.UUID, SESSION);

        verify(sessionService).revoke(Users.UUID, SESSION);
    }
}
