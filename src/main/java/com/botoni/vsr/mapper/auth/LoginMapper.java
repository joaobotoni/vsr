package com.botoni.vsr.mapper.auth;

import com.botoni.vsr.dto.request.auth.LoginRequest;
import com.botoni.vsr.command.auth.SignInCommand;
import com.botoni.vsr.command.session.SessionCommand;
import com.botoni.vsr.dto.response.LoginResponse;
import com.botoni.vsr.dto.response.TokenResponse;
import com.botoni.vsr.entity.users.User;
import com.botoni.vsr.mapper.configuration.MapperConfiguration;
import com.botoni.vsr.mapper.user.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
@Mapper(config = MapperConfiguration.class, uses = UserMapper.class)
public interface LoginMapper {

    SignInCommand toSignIn(LoginRequest login, SessionCommand session);

    @Mapping(target = "user", source = "user")
    @Mapping(target = "token", source = "token")
    LoginResponse toResponse(User user, TokenResponse token);
}
