package com.botoni.vsr.service.token;

import com.botoni.vsr.dto.response.TokenResponse;
import com.botoni.vsr.mapper.token.TokenMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtService jwtService;
    private final TokenMapper tokenMapper;

    public TokenResponse issue(UserDetails user, Integer sid) {
        return tokenMapper.toResponse(jwtService.issue(user, sid), jwtService.expiration());
    }
}