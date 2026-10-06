package com.botoni.vsr.mapper;

import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.dto.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfiguration.class)
public interface UserMapper {

    @Mapping(target = "name", source = "person.name")
    UserResponse toResponse(User user);
}
