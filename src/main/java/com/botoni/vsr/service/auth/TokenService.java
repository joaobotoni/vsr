package com.botoni.vsr.service.auth;

import com.botoni.vsr.dto.response.TokenResponse;
import com.botoni.vsr.entity.users.User;
import com.botoni.vsr.mapper.TokenMapper;
import java.time.Duration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtService jwtService;
    private final TokenMapper tokenMapper;

    public TokenResponse issue(User user) {
        return tokenMapper.toResponse(jwtService.issue(user), expires());
    }

    private long expires() {
        return Duration.ofMillis(jwtService.expirationInMillis()).toSeconds();
    }
}