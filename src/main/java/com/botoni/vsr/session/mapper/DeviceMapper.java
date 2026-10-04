package com.botoni.vsr.session.mapper;

import com.botoni.vsr.session.dto.request.DeviceRequest;
import com.botoni.vsr.session.entity.Device;
import com.botoni.vsr.user.entity.User;
import com.botoni.vsr.infra.configuration.MapperConfiguration;
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
