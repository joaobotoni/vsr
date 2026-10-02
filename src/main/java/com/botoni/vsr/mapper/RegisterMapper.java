package com.botoni.vsr.mapper;

import com.botoni.vsr.dto.request.RegisterRequest;
import com.botoni.vsr.dto.response.RegisterResponse;
import com.botoni.vsr.dto.response.TokenResponse;
import com.botoni.vsr.entity.users.User;
import com.botoni.vsr.mapper.configuration.MapperConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfiguration.class, uses = UserMapper.class)
public interface RegisterMapper {

    @Mapping(target = "person.name", source = "name")
    @Mapping(target = "person.cpf", source = "cpf")
    @Mapping(target = "authorities", ignore = true)
    User toEntity(RegisterRequest request);

    @Mapping(target = "user", source = "user")
    @Mapping(target = "token", source = "token")
    RegisterResponse toResponse(User user, TokenResponse token);
}
