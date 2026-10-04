package com.botoni.vsr.mapper.token;

import com.botoni.vsr.dto.response.TokenResponse;
import com.botoni.vsr.mapper.configuration.MapperConfiguration;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface TokenMapper {
    TokenResponse toResponse(String accessToken, long expiresIn);
}