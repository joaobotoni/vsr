package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.dto.response.TokenResponse;
import com.botoni.vsr.mapper.TokenMapper;
import com.botoni.vsr.token.Claims;
import com.botoni.vsr.token.JwtToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtToken jwtToken;
    private final TokenMapper tokenMapper;

    public TokenResponse issue(User user, Integer session, String refreshToken) {
        String accessToken = sign(user, session);
        return respond(accessToken, refreshToken);
    }

    public Claims verify(String token) {
        return jwtToken.verify(token);
    }

    private String sign(User user, Integer session) {
        return jwtToken.issue(user.getUuid(), session);
    }

    private TokenResponse respond(String accessToken, String refreshToken) {
        return tokenMapper.toResponse(accessToken, refreshToken, jwtToken.expiration());
    }
}
