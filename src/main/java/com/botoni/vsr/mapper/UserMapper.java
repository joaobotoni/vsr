package com.botoni.vsr.mapper;

import com.botoni.vsr.database.entity.Individual;
import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.dto.response.UserResponse;
import com.botoni.vsr.vo.Email;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfiguration.class)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "person", source = "person")
    @Mapping(target = "email", source = "email")
    User entity(Individual person, Email email);

    @Mapping(target = "id", source = "uuid")
    @Mapping(target = "name", source = "person.name")
    UserResponse response(User user);
}
