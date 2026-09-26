package com.botoni.vsr.mapper;

import com.botoni.vsr.dto.request.RegisterRequest;
import com.botoni.vsr.dto.response.LoginResponse;
import com.botoni.vsr.entity.LocalCredential;
import com.botoni.vsr.entity.User;
import com.botoni.vsr.mapper.configuration.MapperConfiguration;
import org.mapstruct.Mapper;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper(config = MapperConfiguration.class)
public interface AuthenticationMapper {

    default LocalCredential toEntity(User user, RegisterRequest request, PasswordEncoder encoder) {
        return LocalCredential.create(user, request.password(), encoder);
    }

    LoginResponse toResponse(String token, long expiresInMs);
}