package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.RefreshToken;
import com.botoni.vsr.database.entity.Session;
import com.botoni.vsr.database.repository.RefreshTokenRepository;
import com.botoni.vsr.exception.custom.RefreshTokenException;
import com.botoni.vsr.exception.enums.problem.RefreshTokenProblem;
import com.botoni.vsr.token.OpaqueToken;
import com.botoni.vsr.support.Controle;
import com.botoni.vsr.support.Tokens;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Refresh tokens")
class RefreshTokenServiceTest {

    private static final int SESSION = 10;

    private final RefreshTokenRepository repository = mock(RefreshTokenRepository.class);
    private final OpaqueToken opaqueToken = new OpaqueToken(Tokens.REFRESH_SECRET);
    private final RefreshTokenService refreshTokenService = new RefreshTokenService(repository, opaqueToken);

    private String token;
    private RefreshToken stored;

    @BeforeEach
    void storedToken() {
        token = opaqueToken.generate();
        stored = RefreshToken.builder().id(SESSION).currentHash(opaqueToken.hash(token))
                .expiresAt(Instant.now().plusSeconds(3600)).build();
        when(repository.findByCurrentHash(any())).thenReturn(Optional.empty());
        when(repository.findByUsedHash(any())).thenReturn(Optional.empty());
    }

    @Test
    @Controle
    @DisplayName("emitir grava só o HMAC do token, nunca o token")
    void issueStoresOnlyHash() {
        String issued = refreshTokenService.issue(Session.builder().id(SESSION).build());

        ArgumentCaptor<byte[]> hash = ArgumentCaptor.forClass(byte[].class);
        verify(repository).issue(eq(SESSION), hash.capture());
        assertThat(hash.getValue()).hasSize(32).isEqualTo(opaqueToken.hash(issued))
                .isNotEqualTo(issued.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @Controle
    @DisplayName("o digest é o HMAC do token, calculado uma vez para busca e reuso")
    void digestIsTokenHmac() {
        assertThat(refreshTokenService.digest(token)).isEqualTo(opaqueToken.hash(token)).hasSize(32);
    }

    @Test
    @Controle
    @DisplayName("token atual é encontrado pelo hash")
    void currentTokenIsFound() {
        when(repository.findByCurrentHash(opaqueToken.hash(token))).thenReturn(Optional.of(stored));

        assertThat(refreshTokenService.find(opaqueToken.hash(token))).isSameAs(stored);
    }

    @Test
    @Controle
    @DisplayName("token já usado também é encontrado, para detectar o reuso")
    void usedTokenIsFound() {
        when(repository.findByUsedHash(opaqueToken.hash(token))).thenReturn(Optional.of(stored));

        assertThat(refreshTokenService.find(opaqueToken.hash(token))).isSameAs(stored);
    }

    @Test
    @Controle
    @DisplayName("token desconhecido é recusado")
    void unknownTokenIsRejected() {
        assertThatThrownBy(() -> refreshTokenService.find(opaqueToken.hash(token)))
                .isInstanceOf(RefreshTokenException.class)
                .extracting("problem").isEqualTo(RefreshTokenProblem.NOT_FOUND);
    }

    @Test
    @Controle
    @DisplayName("renovar troca o hash atual por um novo e devolve outro token")
    void renewRotatesHash() {
        byte[] current = stored.getCurrentHash();

        String renewed = refreshTokenService.renew(stored);

        assertThat(renewed).isNotEqualTo(token);
        verify(repository).renew(SESSION, current, opaqueToken.hash(renewed));
    }

    @Test
    @Controle
    @DisplayName("o token atual não é reuso; qualquer outro é")
    void reuseIsDetectedByHash() {
        assertThat(refreshTokenService.isReused(stored, opaqueToken.hash(token))).isFalse();
        assertThat(refreshTokenService.isReused(stored, opaqueToken.hash(opaqueToken.generate()))).isTrue();
    }
}
