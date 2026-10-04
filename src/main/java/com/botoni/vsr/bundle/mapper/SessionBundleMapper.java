package com.botoni.vsr.bundle.mapper;

import com.botoni.vsr.bundle.SessionBundle;
import com.botoni.vsr.session.dto.request.DeviceRequest;
import com.botoni.vsr.infra.configuration.MapperConfiguration;
import org.mapstruct.Mapper;

import java.net.InetAddress;

@Mapper(config = MapperConfiguration.class)
public interface SessionBundleMapper {

    SessionBundle toBundle(DeviceRequest device, InetAddress ip);
}
