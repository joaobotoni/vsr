package com.botoni.vsr.user.mapper;

import com.botoni.vsr.user.dto.response.UserResponse;
import com.botoni.vsr.user.entity.User;
import com.botoni.vsr.infra.configuration.MapperConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfiguration.class)
public interface UserMapper {

    @Mapping(target = "name", source = "person.name")
    @Mapping(target = "email", source = "email.value")
    UserResponse toResponse(User user);
}
