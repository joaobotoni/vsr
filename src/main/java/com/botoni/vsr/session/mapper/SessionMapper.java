package com.botoni.vsr.session.mapper;

import com.botoni.vsr.bundle.SessionOwnerBundle;
import com.botoni.vsr.session.entity.Device;
import com.botoni.vsr.session.entity.Session;
import com.botoni.vsr.infra.configuration.MapperConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;

@Mapper(config = MapperConfiguration.class)
public interface SessionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "device", source = "device")
    @Mapping(target = "ipAddress", source = "bundle.session.ip")
    @Mapping(target = "expiresAt", source = "expiresAt")
    @Mapping(target = "lastAccessAt", ignore = true)
    @Mapping(target = "revokedAt", ignore = true)
    Session toEntity(Device device, SessionOwnerBundle bundle, Instant expiresAt);
}
