package com.botoni.vsr.service;

import com.botoni.vsr.dto.response.TokenResponse;
import com.botoni.vsr.mapper.TokenMapper;
import com.botoni.vsr.security.JwtToken;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtToken jwtToken;
    private final TokenMapper tokenMapper;

    public TokenResponse issue(UserDetails principal, Integer session, String refreshToken) {
        String accessToken = sign(principal, session);
        return respond(accessToken, refreshToken);
    }

    public JwtToken.Claims verify(String token) {
        return decode(token);
    }

    private String sign(UserDetails principal, Integer session) {
        return jwtToken.issue(principal, session);
    }

    private TokenResponse respond(String accessToken, String refreshToken) {
        return tokenMapper.toResponse(accessToken, refreshToken, jwtToken.expiration());
    }

    private JwtToken.Claims decode(String token) {
        return jwtToken.verify(token);
    }
}
