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

    public TokenResponse issue(UserDetails user, Integer session, String refreshToken) {
        String accessToken = jwtToken.issue(user, session);
        return tokenMapper.toResponse(accessToken, refreshToken, jwtToken.expiration());
    }

    public JwtToken.Claims verify(String token) {
        return jwtToken.verify(token);
    }
}
