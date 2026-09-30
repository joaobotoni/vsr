package com.botoni.vsr.mapper;

import com.botoni.vsr.dto.request.RegisterRequest;
import com.botoni.vsr.dto.response.UserResponse;
import com.botoni.vsr.entity.User;
import com.botoni.vsr.mapper.configuration.MapperConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfiguration.class)
public interface UserMapper {

    @Mapping(target = "person.name", source = "name")
    @Mapping(target = "person.cpf", source = "cpf")
    @Mapping(target = "authorities", ignore = true)
    User toEntity(RegisterRequest request);

    @Mapping(target = "name", source = "person.name")
    @Mapping(target = "email", source = "email.value")
    UserResponse toResponse(User user);
}
