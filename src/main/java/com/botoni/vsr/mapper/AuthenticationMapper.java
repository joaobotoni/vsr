package com.botoni.vsr.mapper;

import com.botoni.vsr.dto.response.LoginResponse;
import com.botoni.vsr.mapper.configuration.MapperConfiguration;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface AuthenticationMapper {
    LoginResponse toLoginResponse(String token, long expiresInMs);
}
