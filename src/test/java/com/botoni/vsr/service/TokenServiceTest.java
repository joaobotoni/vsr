package com.botoni.vsr.service;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.botoni.vsr.dto.response.TokenResponse;
import com.botoni.vsr.mapper.TokenMapper;
import com.botoni.vsr.support.Controle;
import com.botoni.vsr.support.Tokens;
import com.botoni.vsr.support.Users;
import com.botoni.vsr.token.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Emissão do par de tokens")
class TokenServiceTest {

    private static final int SESSION = 10;
    private static final long LIFETIME_SECONDS = 15 * 60;

    private final TokenService tokenService = new TokenService(Tokens.jwt(), Mappers.getMapper(TokenMapper.class));

    @Test
    @Controle
    @DisplayName("o access token sai com o UUID do usuário e a sessão, junto do refresh token e da validade")
    void issueBuildsTokenPair() {
        TokenResponse response = tokenService.issue(Users.ana(), SESSION, "refresh-token");

        Claims claims = tokenService.verify(response.accessToken());
        assertThat(claims.subject()).isEqualTo(Users.UUID);
        assertThat(claims.session()).isEqualTo(SESSION);
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.expiresIn()).isEqualTo(LIFETIME_SECONDS);
    }

    @Test
    @Controle
    @DisplayName("token assinado com outra chave é recusado na verificação")
    void forgedTokenIsRejected() {
        assertThatThrownBy(() -> tokenService.verify(Tokens.forged())).isInstanceOf(JWTVerificationException.class);
    }
}
