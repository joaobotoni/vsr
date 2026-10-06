package com.botoni.vsr.mapper;

import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.dto.response.AuthenticationResponse;
import com.botoni.vsr.dto.response.TokenResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfiguration.class, uses = UserMapper.class)
public interface AuthenticationMapper {

    @Mapping(target = "user", source = "user")
    @Mapping(target = "token", source = "token")
    AuthenticationResponse toResponse(User user, TokenResponse token);
}
