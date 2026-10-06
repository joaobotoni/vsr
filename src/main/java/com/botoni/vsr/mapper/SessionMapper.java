package com.botoni.vsr.mapper;

import com.botoni.vsr.database.entity.Device;
import com.botoni.vsr.database.entity.Session;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.net.InetAddress;
import java.time.Instant;

@Mapper(config = MapperConfiguration.class)
public interface SessionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "device", source = "device")
    @Mapping(target = "ipAddress", source = "ip")
    @Mapping(target = "expiresAt", source = "expiresAt")
    @Mapping(target = "lastAccessAt", ignore = true)
    @Mapping(target = "revokedAt", ignore = true)
    Session toEntity(Device device, InetAddress ip, Instant expiresAt);
}
