package com.botoni.vsr.mapper;

import com.botoni.vsr.dto.response.RegisterResponse;
import com.botoni.vsr.dto.response.TokenResponse;
import com.botoni.vsr.entity.User;
import com.botoni.vsr.mapper.configuration.MapperConfiguration;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class, uses = UserMapper.class)
public interface RegisterMapper {
    RegisterResponse toResponse(User user, TokenResponse token);
}