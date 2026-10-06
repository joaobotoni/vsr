package com.botoni.vsr.mapper;

import com.botoni.vsr.dto.response.TokenResponse;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface TokenMapper {

    TokenResponse toResponse(String accessToken, String refreshToken, long expiresIn);
}
