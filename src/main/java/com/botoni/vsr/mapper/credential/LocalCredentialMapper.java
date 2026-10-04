package com.botoni.vsr.mapper.credential;

import com.botoni.vsr.command.credential.LocalCredentialCommand;
import com.botoni.vsr.entity.users.LocalCredential;
import com.botoni.vsr.entity.users.User;
import com.botoni.vsr.mapper.configuration.MapperConfiguration;
import com.botoni.vsr.vo.Password;
import com.botoni.vsr.vo.PasswordHash;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;

@Mapper(config = MapperConfiguration.class, imports = Instant.class)
public interface LocalCredentialMapper {

    LocalCredentialCommand toCommand(User user, Password password);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "request.user")
    @Mapping(target = "passwordHash", source = "passwordHash")
    @Mapping(target = "passwordUpdatedAt", expression = "java(Instant.now())")
    LocalCredential toEntity(LocalCredentialCommand request, PasswordHash passwordHash);
}
