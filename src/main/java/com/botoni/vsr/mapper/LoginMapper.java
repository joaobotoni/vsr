package com.botoni.vsr.mapper;

import com.botoni.vsr.dto.response.LoginResponse;
import com.botoni.vsr.dto.response.TokenResponse;
import com.botoni.vsr.entity.users.User;
import com.botoni.vsr.mapper.configuration.MapperConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfiguration.class, uses = UserMapper.class)
public interface LoginMapper {

    @Mapping(target = "user", source = "user")
    @Mapping(target = "token", source = "token")
    LoginResponse toResponse(User user, TokenResponse token);
}
