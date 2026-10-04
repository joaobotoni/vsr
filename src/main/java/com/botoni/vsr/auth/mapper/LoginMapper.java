package com.botoni.vsr.auth.mapper;

import com.botoni.vsr.auth.dto.response.LoginResponse;
import com.botoni.vsr.token.dto.response.TokenResponse;
import com.botoni.vsr.user.entity.User;
import com.botoni.vsr.infra.configuration.MapperConfiguration;
import com.botoni.vsr.user.mapper.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
@Mapper(config = MapperConfiguration.class, uses = UserMapper.class)
public interface LoginMapper {

    @Mapping(target = "user", source = "user")
    @Mapping(target = "token", source = "token")
    LoginResponse toResponse(User user, TokenResponse token);
}
