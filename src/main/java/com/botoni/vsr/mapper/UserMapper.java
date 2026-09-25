package com.botoni.vsr.mapper;

import com.botoni.vsr.dto.request.RegisterRequest;
import com.botoni.vsr.dto.response.UserResponse;
import com.botoni.vsr.entity.User;
import com.botoni.vsr.mapper.configuration.MapperConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfiguration.class)
public interface UserMapper {

    @Mapping(target = "name", source = "person.name")
    @Mapping(target = "cpf", source = "person.cpf")
    UserResponse toResponse(User user);

    @Mapping(target = "person.name", source = "name")
    @Mapping(target = "person.cpf.value", source = "cpf")
    @Mapping(target = "email.value", source = "email")
    @Mapping(target = "withPassword", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    User toEntity(RegisterRequest request);
}
