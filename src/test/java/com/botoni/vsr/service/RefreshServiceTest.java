package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.Device;
import com.botoni.vsr.database.entity.RefreshToken;
import com.botoni.vsr.database.entity.Session;
import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.database.repository.RefreshTokenRepository;
import com.botoni.vsr.dto.request.RefreshRequest;
import com.botoni.vsr.dto.response.TokenResponse;
import com.botoni.vsr.exception.custom.RefreshTokenException;
import com.botoni.vsr.exception.custom.SessionException;
import com.botoni.vsr.exception.enums.problem.RefreshTokenProblem;
import com.botoni.vsr.exception.enums.problem.SessionProblem;
import com.botoni.vsr.token.OpaqueToken;
import com.botoni.vsr.support.Controle;
import com.botoni.vsr.support.Tokens;
import com.botoni.vsr.support.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@DisplayName("Rotação e reuso de refresh token")
class RefreshServiceTest {

    private static final int SESSION = 10;

    private final RefreshTokenRepository repository = mock(RefreshTokenRepository.class);
    private final SessionService sessionService = mock(SessionService.class);
    private final TokenService tokenService = mock(TokenService.class);
    private final OpaqueToken opaqueToken = new OpaqueToken(Tokens.REFRESH_SECRET);
    private final RefreshService refreshService =
            new RefreshService(new RefreshTokenService(repository, opaqueToken), sessionService, tokenService);

    private final User owner = Users.ana();

    private final List<byte[]> used = new ArrayList<>();

    private RefreshToken row;
    private String original;

    @BeforeEach
    void issuedToken() {
        original = opaqueToken.generate();
        row = row(original);
        stubRepositoryOver(row);
        when(sessionService.resume(SESSION)).thenReturn(owner);
        when(tokenService.issue(any(), anyInt(), any())).thenAnswer(call -> new TokenResponse("access", call.getArgument(2), 900));
    }

    @Test
    @Controle
    @DisplayName("renovar devolve um refresh token novo")
    void renewalReturnsNewToken() {
        assertThat(refresh(original)).isNotBlank().isNotEqualTo(original);
    }

    @Test
    @Controle
    @DisplayName("token desconhecido é recusado")
    void unknownTokenIsRejected() {
        assertThatThrownBy(() -> refresh(opaqueToken.generate()))
                .isInstanceOf(RefreshTokenException.class)
                .extracting("problem").isEqualTo(RefreshTokenProblem.NOT_FOUND);
    }

    @Test
    @Controle
    @DisplayName("a sessão é retomada uma vez só, pelo próprio id, sem buscar o dono à parte")
    void sessionIsResumedOnce() {
        refresh(original);

        verify(sessionService).resume(SESSION);
        verifyNoMoreInteractions(sessionService);
    }

    @Test
    @Controle
    @DisplayName("sessão revogada impede a renovação")
    void revokedSessionBlocksRenewal() {
        when(sessionService.resume(SESSION)).thenThrow(new SessionException(SessionProblem.REVOKED));

        assertThatThrownBy(() -> refresh(original)).isInstanceOf(SessionException.class);
        verify(repository, never()).renew(any(), any(), any());
    }

    @Test
    @Controle
    @DisplayName("reusar o token imediatamente anterior revoga a sessão")
    void reusingPreviousTokenRevokesSession() {
        refresh(original);

        assertThatThrownBy(() -> refresh(original))
                .isInstanceOf(RefreshTokenException.class)
                .extracting("problem").isEqualTo(RefreshTokenProblem.REUSED);
        verify(sessionService).invalidate(SESSION);
    }

    @Test
    @Controle
    @DisplayName("reuso de um token de várias rotações atrás revoga a sessão")
    void reuseAfterTwoRotationsRevokesSession() {
        refresh(refresh(original));

        assertThatThrownBy(() -> refresh(original))
                .isInstanceOf(RefreshTokenException.class)
                .extracting("problem").isEqualTo(RefreshTokenProblem.REUSED);
        verify(sessionService).invalidate(SESSION);
    }

    @Test
    @Controle
    @DisplayName("reuso de um token já expirado também revoga a sessão")
    void expiredReuseRevokesSession() {
        refresh(original);
        row.setExpiresAt(Instant.now().minusSeconds(1));

        assertThatThrownBy(() -> refresh(original))
                .isInstanceOf(RefreshTokenException.class)
                .extracting("problem").isEqualTo(RefreshTokenProblem.REUSED);
        verify(sessionService).invalidate(SESSION);
    }

    @Test
    @Controle
    @DisplayName("sessão expirada impede a renovação: a validade é decidida só pela sessão")
    void expiredSessionBlocksRenewal() {
        when(sessionService.resume(SESSION)).thenThrow(new SessionException(SessionProblem.EXPIRED));

        assertThatThrownBy(() -> refresh(original))
                .isInstanceOf(SessionException.class)
                .extracting("problem").isEqualTo(SessionProblem.EXPIRED);
        verify(repository, never()).renew(any(), any(), any());
    }

    private String refresh(String token) {
        return refreshService.refresh(new RefreshRequest(token)).refreshToken();
    }

    private RefreshToken row(String token) {
        Device device = Device.builder().id(1).user(owner).build();
        Session session = Session.builder().id(SESSION).device(device).expiresAt(inDays(30)).build();
        return RefreshToken.builder().id(SESSION).session(session).currentHash(opaqueToken.hash(token)).expiresAt(inDays(30)).build();
    }

    /** Simula as tabelas refresh_token e refresh_token_usado e a procedure renovar_refresh_token. */
    private void stubRepositoryOver(RefreshToken row) {
        when(repository.findByCurrentHash(any())).thenAnswer(call -> matching(call.getArgument(0), row.getCurrentHash()));
        when(repository.findByUsedHash(any())).thenAnswer(call -> usedMatching(call.getArgument(0)));
        doAnswer(call -> {
            byte[] current = call.getArgument(1);
            if (!Arrays.equals(current, row.getCurrentHash())) {
                throw new IllegalStateException("rn_refresh_token_renovado");
            }
            used.add(current);
            row.setCurrentHash(call.getArgument(2));
            row.setRenewedAt(Instant.now());
            return null;
        }).when(repository).renew(anyInt(), any(), any());
    }

    private Optional<RefreshToken> matching(byte[] hash, byte[] stored) {
        return Arrays.equals(hash, stored) ? Optional.of(row) : Optional.empty();
    }

    private Optional<RefreshToken> usedMatching(byte[] hash) {
        return used.stream().anyMatch(old -> Arrays.equals(old, hash)) ? Optional.of(row) : Optional.empty();
    }

    private static Instant inDays(int days) {
        return Instant.now().plus(Duration.ofDays(days));
    }
}
