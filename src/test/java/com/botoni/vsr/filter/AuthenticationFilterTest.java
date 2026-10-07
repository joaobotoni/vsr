package com.botoni.vsr.filter;

import com.botoni.vsr.exception.custom.SessionException;
import com.botoni.vsr.exception.enums.problem.SessionProblem;
import com.botoni.vsr.service.SessionService;
import com.botoni.vsr.support.Controle;
import com.botoni.vsr.support.Requests;
import com.botoni.vsr.support.Tokens;
import com.botoni.vsr.support.Users;
import com.botoni.vsr.support.WebSecurityTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Named.named;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebSecurityTest
@DisplayName("Autenticação por JWT + sessão")
class AuthenticationFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private SessionService sessionService;

    @BeforeEach
    void knownUser() {
        when(userDetailsService.loadUserByUsername(Users.EMAIL)).thenReturn(Users.principal());
    }

    @Test
    @Controle
    @DisplayName("token válido de sessão ativa é aceito")
    void validTokenIsAccepted() throws Exception {
        mockMvc.perform(Requests.me(Tokens.valid())).andExpect(status().isOk());
    }

    @Test
    @Controle
    @DisplayName("rota protegida sem token responde 401")
    void missingTokenIsRejected() throws Exception {
        mockMvc.perform(Requests.me()).andExpect(status().isUnauthorized());
    }

    @Controle
    @ParameterizedTest(name = "token {0} responde 401")
    @MethodSource("invalidTokens")
    void invalidTokenIsRejected(String token) throws Exception {
        mockMvc.perform(Requests.me(token)).andExpect(status().isUnauthorized());
    }

    static Stream<Named<String>> invalidTokens() {
        return Stream.of(
                named("assinado com outra chave", Tokens.forged()),
                named("com alg=none", Tokens.unsigned()),
                named("expirado", Tokens.expired()),
                named("de outro emissor", Tokens.wrongIssuer()),
                named("sem o claim de sessão", Tokens.withoutSession()),
                named("malformado", "abc.def.ghi")
        );
    }

    @Controle
    @ParameterizedTest(name = "cabeçalho \"{0}\" responde 401")
    @ValueSource(strings = {"Basic YW5hOnNlbmhh", "bearer qualquer", "Bearer "})
    void malformedHeaderIsRejected(String header) throws Exception {
        mockMvc.perform(Requests.me().header(HttpHeaders.AUTHORIZATION, header)).andExpect(status().isUnauthorized());
    }

    @Test
    @Controle
    @DisplayName("token de usuário que não existe mais responde 401")
    void deletedUserIsRejected() throws Exception {
        when(userDetailsService.loadUserByUsername(Users.EMAIL)).thenThrow(new UsernameNotFoundException("removido"));

        mockMvc.perform(Requests.me(Tokens.valid())).andExpect(status().isUnauthorized());
    }

    @Test
    @Controle
    @DisplayName("rota pública ignora um Authorization inválido")
    void publicRouteIgnoresInvalidToken() throws Exception {
        mockMvc.perform(Requests.login().header(HttpHeaders.AUTHORIZATION, "Bearer " + Tokens.forged()))
                .andExpect(status().isOk());
    }

    @Test
    @Controle
    @DisplayName("logout revoga a sessão do token")
    void logoutRevokesTokenSession() throws Exception {
        mockMvc.perform(Requests.logout(Tokens.valid())).andExpect(status().isNoContent());

        verify(sessionService).revoke(any(), eq(Tokens.SESSION));
    }

    @Controle
    @ParameterizedTest(name = "sessão {0} responde o mesmo 401 genérico, sem revelar o estado")
    @EnumSource(SessionProblem.class)
    void sessionStateIsHidden(SessionProblem problem) throws Exception {
        doThrow(new SessionException(problem)).when(sessionService).access(any(), eq(Tokens.SESSION));

        mockMvc.perform(Requests.me(Tokens.valid()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("SecurityProblem.INVALID_SESSION"));
    }

    @Test
    @Controle
    @DisplayName("toda requisição confere a sessão; a gravação do último acesso é limitada no banco a cada 5 minutos")
    void everyRequestChecksSession() throws Exception {
        String token = Tokens.valid();
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(Requests.me(token)).andExpect(status().isOk());
        }

        verify(sessionService, times(5)).access(any(), eq(Tokens.SESSION));
    }
}
