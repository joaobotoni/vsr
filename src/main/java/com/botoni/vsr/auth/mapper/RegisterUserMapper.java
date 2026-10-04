package com.botoni.vsr.auth.mapper;

import com.botoni.vsr.auth.dto.request.RegisterRequest;
import com.botoni.vsr.user.entity.User;
import com.botoni.vsr.infra.configuration.MapperConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfiguration.class)
public interface RegisterUserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "person.name", source = "name")
    @Mapping(target = "person.cpf", source = "cpf")
    User toEntity(RegisterRequest request);
}
