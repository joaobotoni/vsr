package com.botoni.vsr.token.mapper;

import com.botoni.vsr.token.dto.response.TokenResponse;
import com.botoni.vsr.infra.configuration.MapperConfiguration;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface TokenMapper {
    TokenResponse toResponse(String accessToken, long expiresIn);
}