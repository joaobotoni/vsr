package com.botoni.vsr.mapper;

import com.botoni.vsr.database.entity.LocalCredential;
import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.vo.PasswordHash;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;

@Mapper(config = MapperConfiguration.class)
public interface LocalCredentialMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "passwordHash", source = "passwordHash")
    @Mapping(target = "passwordUpdatedAt", source = "updatedAt")
    LocalCredential entity(User user, PasswordHash passwordHash, Instant updatedAt);
}
