package com.botoni.vsr.mapper.session;

import com.botoni.vsr.dto.request.session.DeviceRequest;
import com.botoni.vsr.entity.users.Device;
import com.botoni.vsr.entity.users.User;
import com.botoni.vsr.mapper.configuration.MapperConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfiguration.class)
public interface DeviceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "identifier", source = "request.identifier")
    @Mapping(target = "platform", source = "request.platform")
    @Mapping(target = "manufacturer", source = "request.manufacturer")
    @Mapping(target = "model", source = "request.model")
    @Mapping(target = "osVersion", source = "request.osVersion")
    Device toEntity(User user, DeviceRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "identifier", ignore = true)
    void update(@MappingTarget Device device, DeviceRequest request);
}
