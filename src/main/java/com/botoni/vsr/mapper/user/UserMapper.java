package com.botoni.vsr.mapper.user;

import com.botoni.vsr.dto.response.UserResponse;
import com.botoni.vsr.entity.users.User;
import com.botoni.vsr.mapper.configuration.MapperConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfiguration.class)
public interface UserMapper {

    @Mapping(target = "name", source = "person.name")
    @Mapping(target = "email", source = "email.value")
    UserResponse toResponse(User user);
}
