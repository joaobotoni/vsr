package com.botoni.vsr.mapper.auth;

import com.botoni.vsr.dto.request.auth.RegisterRequest;
import com.botoni.vsr.command.auth.SignUpCommand;
import com.botoni.vsr.command.session.SessionCommand;
import com.botoni.vsr.dto.response.RegisterResponse;
import com.botoni.vsr.dto.response.TokenResponse;
import com.botoni.vsr.entity.users.User;
import com.botoni.vsr.mapper.configuration.MapperConfiguration;
import com.botoni.vsr.mapper.user.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
@Mapper(config = MapperConfiguration.class, uses = UserMapper.class)
public interface RegisterMapper {

    SignUpCommand toSignUp(RegisterRequest register, SessionCommand session);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "person.name", source = "name")
    @Mapping(target = "person.cpf", source = "cpf")
    User toEntity(RegisterRequest request);

    @Mapping(target = "user", source = "user")
    @Mapping(target = "token", source = "token")
    RegisterResponse toResponse(User user, TokenResponse token);
}
