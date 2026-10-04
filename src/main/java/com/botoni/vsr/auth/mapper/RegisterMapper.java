package com.botoni.vsr.auth.mapper;

import com.botoni.vsr.auth.dto.response.RegisterResponse;
import com.botoni.vsr.token.dto.response.TokenResponse;
import com.botoni.vsr.user.entity.User;
import com.botoni.vsr.infra.configuration.MapperConfiguration;
import com.botoni.vsr.user.mapper.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
@Mapper(config = MapperConfiguration.class, uses = UserMapper.class)
public interface RegisterMapper {

    @Mapping(target = "user", source = "user")
    @Mapping(target = "token", source = "token")
    RegisterResponse toResponse(User user, TokenResponse token);
}
