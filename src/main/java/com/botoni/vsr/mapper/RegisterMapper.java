package com.botoni.vsr.mapper;

import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.dto.request.RegisterRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfiguration.class)
public interface RegisterMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "person.name", source = "name")
    @Mapping(target = "person.cpf", source = "cpf")
    User toEntity(RegisterRequest request);
}
