package com.botoni.vsr.mapper.session;

import com.botoni.vsr.command.session.AccessCommand;
import com.botoni.vsr.dto.request.session.DeviceRequest;
import com.botoni.vsr.command.session.SessionOwnerCommand;
import com.botoni.vsr.command.session.SessionCommand;
import com.botoni.vsr.entity.users.Device;
import com.botoni.vsr.entity.users.Session;
import com.botoni.vsr.mapper.configuration.MapperConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.net.InetAddress;
import java.time.Instant;

@Mapper(config = MapperConfiguration.class)
public interface SessionMapper {

    SessionCommand toCommand(DeviceRequest device, InetAddress ip);

    @Mapping(target = "user", expression = "java(command.principal().user())")
    @Mapping(target = "session", source = "session")
    SessionOwnerCommand toCommand(AccessCommand command);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "device", source = "device")
    @Mapping(target = "ipAddress", source = "command.session.ip")
    @Mapping(target = "expiresAt", source = "expiresAt")
    @Mapping(target = "lastAccessAt", ignore = true)
    @Mapping(target = "revokedAt", ignore = true)
    Session toEntity(Device device, SessionOwnerCommand command, Instant expiresAt);
}
