package com.botoni.vsr.credential.mapper;

import com.botoni.vsr.bundle.LocalCredentialBundle;
import com.botoni.vsr.credential.entity.LocalCredential;
import com.botoni.vsr.infra.configuration.MapperConfiguration;
import com.botoni.vsr.shared.vo.PasswordHash;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;

@Mapper(config = MapperConfiguration.class, imports = Instant.class)
public interface LocalCredentialMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "bundle.user")
    @Mapping(target = "passwordHash", source = "passwordHash")
    @Mapping(target = "passwordUpdatedAt", expression = "java(Instant.now())")
    LocalCredential toEntity(LocalCredentialBundle bundle, PasswordHash passwordHash);
}
